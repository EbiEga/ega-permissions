package uk.ac.ebi.ega.permissions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.ac.ebi.ega.permissions.mapper.ApiKeyMapper;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;
import uk.ac.ebi.ega.permissions.persistence.repository.ApiKeyRepository;

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

    @BeforeEach
    void setup() {
        apiKeyService = new ApiKeyServiceImpl(keyAlgorithm, pbeAlgorithm, egaPassword, apiKeyMapper, apiKeyRepository);
    }

    @Test
    void generateAndVerifyToken() throws Exception {
        ApiKeyParams params = new ApiKeyParams("user@ebi.ac.uk", "MyTestKeyID", new Date(), "Reason");
        params = apiKeyService.generateKeys(params);

        ApiKey apiKey = new ApiKey(params.getUsername(), params.getKeyId(), params.getExpiration(), params.getReason(), params.getSalt(), params.getPrivateKey());

        when(apiKeyRepository.findApiKeyByUsernameAndKeyName(any(), any())).thenReturn(Optional.of(apiKey));

        assertThat(apiKeyService.verifyToken(params.getToken())).isTrue();
    }
}