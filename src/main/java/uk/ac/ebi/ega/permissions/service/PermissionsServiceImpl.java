package uk.ac.ebi.ega.permissions.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public PermissionsServiceImpl(PermissionsDataService permissionsDataService, TokenPayloadMapper tokenPayloadMapper) {
        this.permissionsDataService = permissionsDataService;
        this.tokenPayloadMapper = tokenPayloadMapper;
    }

    @Override
    public boolean accountExist(String accountId) {
        return this.permissionsDataService.getTokenPayloadForAccount(accountId) != null;
    }

    @Override
    public List<Visa> getVisas(String accountId) {
        List<PassportVisaObject> passportVisaObjects = this.tokenPayloadMapper.mapPassportClaims(this.permissionsDataService.getPassPortClaimsForAccount(accountId));
        Visa visa = this.tokenPayloadMapper.mapTokenPayload(this.permissionsDataService.getTokenPayloadForAccount(accountId));

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
        PassportClaim savedClaim = this.permissionsDataService.savePassportClaim(accountId, tokenPayloadMapper.mapPassportVisaObject(passportVisaObject));
        return this.tokenPayloadMapper.mapPassportClaim(savedClaim);
    }

    @Override
    public int deletePassportVisaObject(String accountId, String value) {
        return this.permissionsDataService.deletePassportClaim(accountId, value);
    }
}
