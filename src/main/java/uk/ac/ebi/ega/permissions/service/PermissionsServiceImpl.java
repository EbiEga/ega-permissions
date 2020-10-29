package uk.ac.ebi.ega.permissions.service;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.ega.permissions.configuration.VisaInfoProperties;
import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;

import javax.persistence.PersistenceException;
import javax.validation.ValidationException;
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
    public Optional<Account> getAccountByEmail(String email) {
        return permissionsDataService.getAccountByEmail(email);
    }

    @Override
    public Optional<AccountElixirId> getAccountIdForElixirId(String elixirId) {
        return permissionsDataService.getAccountIdForElixirId(elixirId);
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
    public PassportVisaObject savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject) throws ServiceException, SystemException {
        try {
            //TODO: This will be improved later with other validations such as valid accountIds and Datasets
            if (passportVisaObject.getValue().isEmpty() || accountId.isEmpty()) {
                throw new ValidationException("Values for accountId and value are incorrect or not valid");
            }
            PassportClaim savedClaim = this.permissionsDataService.savePassportClaim(tokenPayloadMapper.mapPassportVisaObjectToPassportClaim(accountId, passportVisaObject));
            passportVisaObject = this.tokenPayloadMapper.mapPassportClaimToPassportVisaObject(savedClaim);
        } catch (PersistenceException | CannotCreateTransactionException | IllegalArgumentException ex) { //These are spring-data possible errors
            throw new ServiceException(String.format("Error saving permissions to the DB for [account:%s, object:%s]", accountId, passportVisaObject.getValue()), ex);
        } catch (ValidationException exception) {
            throw exception;
        } catch (Exception ex) { //Generic errors are wrapped with a default message
            throw new SystemException(String.format("Error processing permissions for [account:%s, object:%s]", accountId, passportVisaObject.getValue()), ex);
        }
        return passportVisaObject;
    }

    @Override
    public int deletePassportVisaObject(String accountId, String value) {
        return this.permissionsDataService.deletePassportClaim(accountId, value);
    }

    @Override
    public List<AccountAccess> getGrantedAccountsForDataset(String datasetId){
        return this.tokenPayloadMapper.mapPassportClaimsToAccountAccesses(this.permissionsDataService.getPassportClaimsForDataset(datasetId));
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
