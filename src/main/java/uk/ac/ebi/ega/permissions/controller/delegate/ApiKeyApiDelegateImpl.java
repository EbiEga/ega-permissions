/*
 * Copyright 2021-2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.ega.permissions.controller.delegate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.ega.permissions.api.ApiKeyApiDelegate;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.service.ApiKeyService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ApiKeyApiDelegateImpl implements ApiKeyApiDelegate {

    private final ApiKeyService apiKeyService;

    public ApiKeyApiDelegateImpl(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public ResponseEntity<CreatedAPIKey> generateApiKey(String id, String expirationDate, String reason) {

        String username = getUsername();
        Date expiration;

        try {
            expiration = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate);
            return ResponseEntity.ok(this.apiKeyService.createApiKey(new ApiKeyParams(username, id, expiration, reason)));
        } catch (ParseException e) {
            throw new SystemException("Error parsing expiration date");
        } catch (Exception e) {
            throw new SystemException("Error generating the API_KEY for user: " + username);
        }
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
