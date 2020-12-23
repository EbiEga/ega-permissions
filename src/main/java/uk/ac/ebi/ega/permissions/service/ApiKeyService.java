package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;

import java.util.List;

public interface ApiKeyService {

    CreatedAPIKey createApiKey(ApiKeyParams params) throws Exception;

    void saveApiKeyDetails(ApiKeyParams params);

    ApiKeyParams generateKeys(ApiKeyParams params) throws Exception;

    List<APIKeyListItem> getApiKeysForUser(String username);

    void deleteApiKey(String username, String keyId);

    boolean verifyToken(String token) throws Exception;

}
