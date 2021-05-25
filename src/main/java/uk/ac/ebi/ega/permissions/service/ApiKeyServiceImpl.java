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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.ga4gh.jwt.passport.exception.SystemException;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.ApiKey;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.ApiKeyId;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.repository.ApiKeyRepository;
import uk.ac.ebi.ega.permissions.mapper.ApiKeyMapper;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.transaction.Transactional;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ApiKeyServiceImpl implements ApiKeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyServiceImpl.class);

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
    public CreatedAPIKey createApiKey(ApiKeyParams params) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        params = this.generateKeys(params);
        this.saveApiKeyDetails(params);
        return apiKeyMapper.fromApiKeyParams(params);
    }

    @Override
    public void saveApiKeyDetails(ApiKeyParams params) {
        ApiKey apiKey = new ApiKey(new ApiKeyId(params.getUsername(), params.getKeyId()), params.getExpiration(), params.getReason(),
                params.getSalt(), params.getPrivateKey());
        this.apiKeyRepository.save(apiKey);
    }

    @Override
    public ApiKeyParams generateKeys(ApiKeyParams params) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
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

    /***
     *
     * @param token Encrypted API_KEY
     * @return Optional containing the username if the API_KEY is valid, empty otherwise
     */
    @Override
    public Optional<String> getUserFromToken(String token) {
        String username = null;
        String keyId = null;

        try {
            byte[] decryptedToken = encryptionUtils.decryptWithPassword(egaPassword.getBytes(), decodeString(token));

            String[] tokenParts = new String(decryptedToken).split("\\.");

            assertTokenLength(tokenParts);

            username = new String(decodeString(tokenParts[0]));
            keyId = new String(decodeString(tokenParts[1]));
            String encryptedSalt = tokenParts[2];

            ApiKey apiKey = this.apiKeyRepository.findApiKeyByUsernameAndKeyName(username, keyId)
                    .orElseThrow(() -> new SystemException("The API_KEY is not valid"));

            assertTokenExpiration(apiKey);

            byte[] decryptedSalt = encryptionUtils.decryptWithKey(decodeString(apiKey.getPrivateKey()), decodeString(encryptedSalt));

            assertSaltIsValid(encodeBytes(decryptedSalt), apiKey.getSalt());
            return Optional.of(username);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException exception) {
            LOGGER.error("Internal error trying to verify API_KEY Token. Username: {} - keyId:{}", username, keyId);
            return Optional.empty();
        } catch (AssertionError exception) {
            LOGGER.warn("Invalid API_KEY found in request. Username: {} - KeyId:{}", username, keyId);
            return Optional.empty();
        } catch (Exception exception) {
            LOGGER.error("An unexpected error occurred trying to verify an API_KEY. Username: {} - KeyId:{}", username, keyId);
            return Optional.empty();
        }

    }

    private void assertSaltIsValid(String decryptedSalt, String savedSalt) {
        assert decryptedSalt.equals(savedSalt) : "The API_KEY is invalid.";
    }

    private void assertTokenLength(String[] tokenParts) {
        assert tokenParts.length == 3 : "API_KEY token length is invalid";
    }

    private void assertTokenExpiration(ApiKey apiKey) {
        assert apiKey.getExpiration().after(new Date()) : "API_KEY is invalid (Expired)";
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
