package uk.ac.ebi.ega.permissions.service;

import org.springframework.util.CollectionUtils;
import uk.ac.ebi.ega.permissions.configuration.VisaInfoProperties;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PermissionsServiceImpl implements PermissionsService {

    private PermissionsDataService permissionsDataService;
    private TokenPayloadMapper tokenPayloadMapper;
    private VisaInfoProperties visaInfoProperties;

    public PermissionsServiceImpl(PermissionsDataService permissionsDataService, TokenPayloadMapper tokenPayloadMapper, VisaInfoProperties visaInfoProperties) {
        this.permissionsDataService = permissionsDataService;
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.visaInfoProperties = visaInfoProperties;
    }

    @Override
    public boolean accountExist(String accountId) {
        return this.permissionsDataService.accountExists(accountId);
    }

    @Override
    public List<Visa> getVisas(String accountId) {
        List<PassportVisaObject> passportVisaObjects = this.tokenPayloadMapper
                .mapPassportClaimsToPassportVisaObjects(this.permissionsDataService.getPassPortClaimsForAccount(accountId));

        if (CollectionUtils.isEmpty(passportVisaObjects)) {
            return Collections.emptyList();
        }

        Visa visa = generatedVisaInfo(accountId);

        List<Visa> visas = passportVisaObjects.stream().map(e -> {
            Visa innerVisa = new Visa();
            innerVisa.setJti(visa.getJti());
            innerVisa.setIss(visa.getIss());
            innerVisa.setExp(visa.getExp());
            innerVisa.setSub(visa.getSub());
            innerVisa.setIat(visa.getIat());
            innerVisa.setGa4ghVisaV1(e);
            return innerVisa;
        }).collect(Collectors.toList());

        return visas;
    }

    @Override
    public Optional<PassportVisaObject> savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject) {
        PassportVisaObject savedObject;
        try {
            PassportClaim savedClaim = this.permissionsDataService.savePassportClaim(tokenPayloadMapper.mapPassportVisaObjectToPassportClaim(accountId, passportVisaObject));
            savedObject = this.tokenPayloadMapper.mapPassportClaimToPassportVisaObject(savedClaim);
            return Optional.of(savedObject);
        } catch (Exception ex) {
            //Handle exception in the save process so the caller can check using Optional
            return Optional.empty();
        }
    }

    @Override
    public int deletePassportVisaObject(String accountId, String value) {
        return this.permissionsDataService.deletePassportClaim(accountId, value);
    }

    //TODO: Verify/improve this logic to populate visa attributes
    // this can be generated but for now I'm using values from properties
    private Visa generatedVisaInfo(String accountId) {
        Visa visa = new Visa();
        visa.setSub(accountId);
        visa.setIss(this.visaInfoProperties.getIssuer());
        visa.setExp(Calendar.getInstance().getTimeInMillis() / 1000L + this.visaInfoProperties.getExpireAfter());
        visa.setIat(this.visaInfoProperties.getIat());
        visa.setJti(UUID.randomUUID().toString());
        return visa;
    }
}
