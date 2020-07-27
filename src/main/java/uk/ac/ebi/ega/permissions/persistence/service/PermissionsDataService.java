package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.TokenPayload;

import java.util.List;

public interface PermissionsDataService {

    List<PassportClaim> getPassPortClaimsForAccount(String accountId);

    PassportClaim savePassportClaim(String accountId, PassportClaim claim);

    int deletePassportClaim(String accountId, String value);

    TokenPayload getTokenPayloadForAccount(String accountId);

}
