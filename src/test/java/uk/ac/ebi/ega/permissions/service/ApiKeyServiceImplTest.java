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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.ac.ebi.ega.permissions.mapper.ApiKeyMapper;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;
import uk.ac.ebi.ega.permissions.persistence.repository.ApiKeyRepository;
import uk.ac.ebi.ega.permissions.utils.EncryptionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiKeyServiceImplTest {

    private final String keyAlgorithm = "RSA";
    private final String pbeAlgorithm = "AES";
    private final String egaPassword = "Bar12345Bar12345";

    private ApiKeyMapper apiKeyMapper = Mappers.getMapper(ApiKeyMapper.class);
    private ApiKeyRepository apiKeyRepository = mock(ApiKeyRepository.class);

    private ApiKeyService apiKeyService;
    private EncryptionUtils encryptionUtils;

    @BeforeEach
    void setup() {
        encryptionUtils = new EncryptionUtils(keyAlgorithm, pbeAlgorithm);
        apiKeyService = new ApiKeyServiceImpl(egaPassword, apiKeyMapper, apiKeyRepository, encryptionUtils);
    }

    @Test
    void generateAndVerifyToken_OK() throws Exception {
        Date futureDate = Date.from(Instant.now().plus(Duration.ofHours(1)));

        ApiKeyParams params = new ApiKeyParams("user@ebi.ac.uk", "MyTestKeyID", futureDate, "Reason");
        params = apiKeyService.generateKeys(params);

        ApiKey apiKey = new ApiKey(params.getUsername(), params.getKeyId(), params.getExpiration(), params.getReason(), params.getSalt(), params.getPrivateKey());

        when(apiKeyRepository.findApiKeyByUsernameAndKeyName(any(), any())).thenReturn(Optional.of(apiKey));

        assertThat(apiKeyService.verifyToken(params.getToken())).isTrue();
    }

    @Test
    void generateAndVerifyToken_Expired() throws Exception {
        Date pastDate = Date.from(Instant.now().minus(Duration.ofHours(1)));

        ApiKeyParams inputParams = new ApiKeyParams("user@ebi.ac.uk", "MyTestKeyID", pastDate, "Reason");
        ApiKeyParams encryptedParams = apiKeyService.generateKeys(inputParams);

        ApiKey apiKey = new ApiKey(inputParams.getUsername(), inputParams.getKeyId(), inputParams.getExpiration(), inputParams.getReason(), encryptedParams.getSalt(), encryptedParams.getPrivateKey());

        when(apiKeyRepository.findApiKeyByUsernameAndKeyName(any(), any())).thenReturn(Optional.of(apiKey));

        assertThat(apiKeyService.verifyToken(encryptedParams.getToken())).isFalse();
    }


}
