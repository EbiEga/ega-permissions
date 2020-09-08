package uk.ac.ebi.ega.permissions.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;

import java.net.URI;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
class PermissionsControllerPlainIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int port;

    @Test
    @DisplayName("NOT_FOUND Response when GET request sent to /plain/{accountId}/permissions endpoint")
    public void shouldReturnNotFoundWithInvalidUserAccountId() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/plain/EGAW0000001000/permissions";
        URI uri = new URI(baseUrl);
        ResponseEntity result = this.restTemplate.getForEntity(uri, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /plain/{accountId}/permissions endpoint")
    public void shouldReturnOkWithUserPermissions() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/plain/EGAW0000002000/permissions";
        URI uri = new URI(baseUrl);

        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setBy("dac");

        this.restTemplate.postForEntity(uri, Arrays.asList(passportVisaObject), PermissionsResponse[].class);

        ResponseEntity<Visa[]> result = this.restTemplate.getForEntity(uri, Visa[].class);
        Visa[] permissions = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(1);
    }

    @Test
    @DisplayName("MULTI_STATUS Response when POST request sent to /plain/{accountId}/permissions endpoint")
    public void shouldReturnMultiStatusWithResponses() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/plain/EGAW0000003000/permissions";
        URI uri = new URI(baseUrl);

        PassportVisaObject passportVisaObject1 = new PassportVisaObject();
        passportVisaObject1.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject1.setType("ControlledAccessGrants");
        passportVisaObject1.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject1.setAsserted(1568814383L);
        passportVisaObject1.setBy("dac");

        PassportVisaObject passportVisaObject2 = new PassportVisaObject();
        passportVisaObject2.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject2.setType("ControlledAccessGrants");
        passportVisaObject2.setValue("https://ega-archive.org/datasets/EGAD00003333333");
        passportVisaObject2.setAsserted(1568814383L);
        passportVisaObject2.setBy("dac");

        ResponseEntity<PermissionsResponse[]> result = this.restTemplate.postForEntity(uri, Arrays.asList(passportVisaObject1, passportVisaObject2),
                PermissionsResponse[].class);
        PermissionsResponse[] responses = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(responses).hasSize(2);

        assertThat(responses).filteredOn(e -> e.getStatus() == HttpStatus.CREATED.value()).hasSize(2);
    }

    @Test
    @DisplayName("OK Response when DELETE request sent to /plain/{accountId}/permissions endpoint")
    public void shouldReturnOkWithNoResponseBody() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/plain/EGAW0000004000/permissions";
        URI uri = new URI(baseUrl);

        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setBy("dac");

        this.restTemplate.postForEntity(uri, Arrays.asList(passportVisaObject), PermissionsResponse[].class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("value", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("NOT_FOUND Response when DELETE request sent to /plain/{accountId}/permissions endpoint")
    public void shouldReturnNotFoundForDeleteOperation() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/plain/EGAW0000005000/permissions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("value", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}