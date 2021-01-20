/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.ApiKeyMapper;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;
import uk.ac.ebi.ega.permissions.persistence.repository.ApiKeyRepository;
import uk.ac.ebi.ega.permissions.utils.EncryptionUtils;

import javax.transaction.Transactional;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class ApiKeyServiceImpl implements ApiKeyService {

    private final String egaPassword;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyRepository apiKeyRepository;
    private final EncryptionUtils encryptionUtils;

    public ApiKeyServiceImpl(String egaPassword, ApiKeyMapper apiKeyMapper,
                             ApiKeyRepository apiKeyRepository, EncryptionUtils encryptionUtils) {
        this.egaPassword = egaPassword;
        this.apiKeyMapper = apiKeyMapper;
        this.apiKeyRepository = apiKeyRepository;
        this.encryptionUtils = encryptionUtils;
    }

    @Override
    public CreatedAPIKey createApiKey(ApiKeyParams params) throws Exception {
        params = this.generateKeys(params);
        this.saveApiKeyDetails(params);
        return apiKeyMapper.fromApiKeyParams(params);
    }

    @Override
    public void saveApiKeyDetails(ApiKeyParams params) {
        ApiKey apiKey = new ApiKey(params.getUsername(), params.getKeyId(), params.getExpiration(), params.getReason(),
                params.getSalt(), params.getPrivateKey());
        this.apiKeyRepository.save(apiKey);
    }

    @Override
    public ApiKeyParams generateKeys(ApiKeyParams params) throws Exception {
        //Generate public and private keys
        KeyPair generateKeyPair = encryptionUtils.generateKeyPair();
        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();

        //Generate and encrypt salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[20];
        random.nextBytes(salt);

        byte[] encryptedSalt = encryptionUtils.encryptWithKey(publicKey, salt);

        params.setPrivateKey(encodeBytes(privateKey));
        params.setSalt(encodeBytes(salt));

        String userAndKeyAndEncryptedSalt = encodeString(params.getUsername()) + "." + encodeString(params.getKeyId()) + "." + encodeBytes(encryptedSalt);

        byte[] encryptedToken = encryptionUtils.encryptWithPassword(egaPassword.getBytes(), userAndKeyAndEncryptedSalt.getBytes());
        params.setToken(encodeBytes(encryptedToken));

        return params;
    }

    @Override
    public List<APIKeyListItem> getApiKeysForUser(String username) {
        return this.apiKeyMapper.fromEntityList(this.apiKeyRepository.findAllByUsername(username));
    }

    @Override
    @Transactional
    public void deleteApiKey(String username, String keyId) {
        this.apiKeyRepository.removeAllByUsernameAndKeyName(username, keyId);
    }

    @Override
    public boolean verifyToken(String token) throws Exception {
        byte[] decryptedToken = encryptionUtils.decryptWithPassword(egaPassword.getBytes(), decodeString(token));
        String[] tokenParts = new String(decryptedToken).split("\\.");

        if (tokenParts.length != 3) {
            throw new SystemException("Error verifying the API_KEY");
        }

        String username = tokenParts[0];
        String keyId = tokenParts[1];
        String encryptedSalt = tokenParts[2];

        ApiKey apiKey = this.apiKeyRepository.findApiKeyByUsernameAndKeyName(username, keyId)
                .orElseThrow(() -> new SystemException("The API_KEY is not valid"));

        if(apiKey.getExpiration().before(new Date())){
            return false; // Token expired
        }

        byte[] decryptedSalt = encryptionUtils.decryptWithKey(decodeString(apiKey.getPrivateKey()), decodeString(encryptedSalt));
        return encodeBytes(decryptedSalt).equals(apiKey.getSalt());
    }

    private String encodeBytes(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    private String encodeString(String input) {
        return encodeBytes(input.getBytes());
    }

    private byte[] decodeString(String input) {
        return Base64.getDecoder().decode(input);
    }

}
