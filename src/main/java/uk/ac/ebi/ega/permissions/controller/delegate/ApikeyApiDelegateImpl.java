package uk.ac.ebi.ega.permissions.controller.delegate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.ega.permissions.api.ApikeyApiDelegate;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.service.ApiKeyService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ApikeyApiDelegateImpl implements ApikeyApiDelegate {

    private final ApiKeyService apiKeyService;

    public ApikeyApiDelegateImpl(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public ResponseEntity<CreatedAPIKey> generateApiKey(String id, String expirationDate, String reason) {

        String username = getUsername();
        Date expiration;
        try {
            expiration = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate);
        } catch (ParseException e) {
            throw new SystemException("Error parsing expiration date");
        }

        CreatedAPIKey apiKey = null;
        try {
            apiKey = this.apiKeyService.createApiKey(new ApiKeyParams(username, id, expiration, reason));
        } catch (Exception e) {
            throw new SystemException("Error generating the API_KEY for user: " + username);
        }

        return ResponseEntity.ok(apiKey);
    }

    @Override
    public ResponseEntity<List<APIKeyListItem>> getApiKeys() {
        return ResponseEntity.ok(this.apiKeyService.getApiKeysForUser(getUsername()));
    }

    @Override
    public ResponseEntity<Void> deleteApiKey(String key) {
        this.apiKeyService.deleteApiKey(getUsername(), key);
        return ResponseEntity.ok().build();
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new SystemException("Can't retrieve API_KEY list for Anonymous users");
        }
        return authentication.getName();
    }
}
