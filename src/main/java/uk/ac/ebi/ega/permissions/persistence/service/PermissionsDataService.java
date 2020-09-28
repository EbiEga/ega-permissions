package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.util.List;

public interface PermissionsDataService {

    List<PassportClaim> getPassPortClaimsForAccount(String accountId);

    PassportClaim savePassportClaim(PassportClaim claim);

    int deletePassportClaim(String accountId, String value);

    boolean accountExists(String accountId);

    List<PassportClaim> getPassportClaimsForDataset(String datasetId);
}
