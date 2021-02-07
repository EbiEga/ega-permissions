package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.util.List;
import java.util.Optional;

public interface PermissionsDataService {

    List<PassportClaim> getPassPortClaimsForAccount(String accountId);

    Optional<Account> getAccountByEmail(String email);

    Optional<AccountElixirId> getAccountIdForElixirId(String elixirId);

    PassportClaim savePassportClaim(PassportClaim claim);

    Optional<PassportClaim> deletePassportClaim(String accountId, String value);

    boolean accountExists(String accountId);

    List<PassportClaim> getPassportClaimsForDataset(String datasetId);

    Optional<PassportClaim> getPassportClaimByAccountIdAndValue(String accountId, String value);
}
