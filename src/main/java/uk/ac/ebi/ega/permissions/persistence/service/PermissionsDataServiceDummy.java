package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.TokenPayload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionsDataServiceDummy implements PermissionsDataService {

    private Map<String, TokenPayload> permissionsStore = new HashMap<>();

    @Override
    public List<PassportClaim> getPassPortClaimsForAccount(String accountId) {
        List<PassportClaim> passportClaims = null;
        if (this.permissionsStore.containsKey(accountId)) {
            passportClaims = this.permissionsStore.get(accountId).getClaims();
        }
        return passportClaims;
    }

    @Override
    public PassportClaim savePassportClaim(String accountId, PassportClaim claim) {
        //Simulating validations that can be introduced with JPA. Requests with empty values will return responses other than 200
        if (claim.getValue().isEmpty()) {
            return null;
        }
        if (this.permissionsStore.containsKey(accountId)) {
            TokenPayload tokenPayload = this.permissionsStore.get(accountId);
            tokenPayload.getClaims().add(claim);
        } else {
            //TODO: The additional token information (sub, iss, iat, jti) doest not come with the request. Need to define this.
            TokenPayload tokenPayload = new TokenPayload(accountId, "https://ega.ebi.ac.uk:8053/ega-openid-connect-server/",
                    1592824514, "f030c620-993b-49af-a830-4b9af4f379f8", 1592820914, new ArrayList<>(Arrays.asList(claim)));
            this.permissionsStore.put(accountId, tokenPayload);
        }
        return claim;
    }

    @Override
    public int deletePassportClaim(String accountId, String value) {
        int result = 0;
        if (this.permissionsStore.containsKey(accountId)) {
            TokenPayload tokenPayload = this.permissionsStore.get(accountId);
            List<PassportClaim> claims = tokenPayload.getClaims().stream().filter(e -> e.getValue().equals(value)).collect(Collectors.toList());
            result = claims.size();
            tokenPayload.getClaims().removeAll(claims);
        }
        return result;
    }

    @Override
    public TokenPayload getTokenPayloadForAccount(String accountId) {
        return this.permissionsStore.get(accountId);
    }
}
