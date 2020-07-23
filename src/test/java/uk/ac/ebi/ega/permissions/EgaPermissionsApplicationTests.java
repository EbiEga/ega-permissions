package uk.ac.ebi.ega.permissions;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EgaPermissionsApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    public void canGetVersionFromService()
            throws IOException {

        // Given
        HttpUriRequest request = new HttpGet("http://localhost:" + port + "/version");

        // When
        try (CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(request)) {

            // Then
            Assertions.assertEquals(HttpStatus.OK.value(), httpResponse.getStatusLine().getStatusCode());

            Assertions.assertEquals(String.format("{\"version\":\"%s\"}", VersionController.version), IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
        }
    }


}


