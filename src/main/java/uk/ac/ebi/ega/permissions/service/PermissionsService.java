package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;

import java.util.List;
import java.util.Optional;

public interface PermissionsService {

    boolean accountExist(String accountId);

    Optional<AccountElixirId> getAccountIdForElixirId(String elixirId);

    Optional<Account> getAccountByEmail(String email);

    List<Visa> getVisas(String accountId);

    PassportVisaObject savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject) throws ServiceException, SystemException;

    void deletePassportVisaObject(String accountId, String value);

    List<AccountAccess> getGrantedAccountsForDataset(String datasetId);
}
