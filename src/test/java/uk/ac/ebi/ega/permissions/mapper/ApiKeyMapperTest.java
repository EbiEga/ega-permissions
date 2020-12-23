package uk.ac.ebi.ega.permissions.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyMapperTest {

    ApiKeyMapper mapper = Mappers.getMapper(ApiKeyMapper.class);

    @Test
    void fromApiKeyParams() throws ParseException {
        Date testDate = new SimpleDateFormat("dd/MM/yyyy").parse("18/03/1990");
        ApiKeyParams params = new ApiKeyParams("username", "keyId", testDate, "reason");

        CreatedAPIKey apiKey = mapper.fromApiKeyParams(params);

        assertThat(apiKey.getExpirationDate()).isEqualTo(testDate.getTime());
        assertThat(apiKey.getId()).isEqualTo("keyId");
        assertThat(apiKey.getToken()).isNull();

        params.setToken("dummy-token-test");
        apiKey = mapper.fromApiKeyParams(params);
        assertThat(apiKey.getToken()).isEqualTo("dummy-token-test");
    }

    @Test
    void fromEntity() throws ParseException {
        Date testDate = new SimpleDateFormat("dd/MM/yyyy").parse("18/03/1990");
        ApiKey apiKey = new ApiKey("username", "keyName", testDate, "reason", "salt", "privateKey");

        APIKeyListItem listItem = mapper.fromEntity(apiKey);

        assertThat(listItem.getId()).isEqualTo("keyName");
        assertThat(listItem.getExpirationDate()).isEqualTo(testDate.getTime());
        assertThat(listItem.getReason()).isEqualTo("reason");
    }
}