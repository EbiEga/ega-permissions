package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;

import java.util.List;
import java.util.stream.Collectors;

public class PermissionsServiceImpl implements PermissionsService {

    private PermissionsDataService permissionsDataService;
    private TokenPayloadMapper tokenPayloadMapper;

    public PermissionsServiceImpl(PermissionsDataService permissionsDataService, TokenPayloadMapper tokenPayloadMapper) {
        this.permissionsDataService = permissionsDataService;
        this.tokenPayloadMapper = tokenPayloadMapper;
    }

    @Override
    public boolean accountExist(String accountId) {
        return this.permissionsDataService.accountExists(accountId);
    }

    @Override
    public List<Visa> getVisas(String accountId) {
        List<PassportVisaObject> passportVisaObjects = this.tokenPayloadMapper
                .mapPassportClaimsToPassportVisaObjects(this.permissionsDataService.getPassPortClaimsForAccount(accountId));
        Visa visa = generatedVisa();

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
    public PassportVisaObject savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject) {
        PassportClaim savedClaim = this.permissionsDataService
                .savePassportClaim(tokenPayloadMapper.mapPassportVisaObjectToPassportClaim(accountId, passportVisaObject));
        return this.tokenPayloadMapper.mapPassportClaimToPassportVisaObject(savedClaim);
    }

    @Override
    public int deletePassportVisaObject(String accountId, String value) {
        return this.permissionsDataService.deletePassportClaim(accountId, value);
    }

    //TODO: Implement logic to populate visa attributes
    private Visa generatedVisa() {
        Visa visa = new Visa();
        visa.setSub("EGAW00000015388");
        visa.setIss("https://ega.ebi.ac.uk:8053/ega-openid-connect-server/");
        visa.setExp(1592824514);
        visa.setIat(1592820914);
        visa.setJti("f030c620-993b-49af-a830-4b9af4f379f8");
        return visa;
    }
}
