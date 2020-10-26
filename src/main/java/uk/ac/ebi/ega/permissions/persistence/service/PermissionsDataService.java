package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.util.List;
import java.util.Optional;

public interface PermissionsDataService {

    List<PassportClaim> getPassPortClaimsForAccount(String accountId);

    Optional<Account> getAccountByEmail(String email);

    PassportClaim savePassportClaim(PassportClaim claim);

    int deletePassportClaim(String accountId, String value);

    boolean accountExists(String accountId);

    List<PassportClaim> getPassportClaimsForDataset(String datasetId);
}
