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
import uk.ac.ebi.ega.permissions.model.JWTTokenResponse;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;

import java.net.URI;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class PermissionsControllerJwtIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int port;

    @Test
    @DisplayName("NOT_FOUND Response when GET request sent to /jwt/{accountId}/permissions endpoint")
    public void shouldReturnNotFoundWithInvalidUserAccountId() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/jwt/EGAW0000001000/permissions";
        URI uri = new URI(baseUrl);
        ResponseEntity result = this.restTemplate.getForEntity(uri, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /jwt/{accountId}/permissions endpoint")
    public void shouldReturnOkWithUserPermissions() throws Exception {
        String baseUrl = "http://localhost:" + port + "/plain/EGAW0000002000/permissions";

        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setBy("dac");

        this.restTemplate.postForEntity(new URI(baseUrl), Arrays.asList(passportVisaObject), PermissionsResponse[].class);

        baseUrl = "http://localhost:" + port + "/jwt/EGAW0000002000/permissions";
        ResponseEntity<String[]> result = this.restTemplate.getForEntity(new URI(baseUrl), String[].class);
        String[] permissions = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(1);
    }

    @Test
    @DisplayName("MULTI_STATUS Response when POST request sent to /jwt/{accountId}/permissions endpoint")
    public void shouldReturnMultiStatusWithResponses() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/jwt/EGAW0000003000/permissions";
        URI uri = new URI(baseUrl);

        String jwt1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWlsaW8iLCJnYTRnaF92aXNhX3YxIjp7ImFzc2VydGVkIjoxNTY4ODE0MzgzLCJieSI6ImRhYyIsInNvdXJjZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhY3MvRUdBQzAwMDAxMDAwNTE0IiwidHlwZSI6IkNvbnRyb2xsZWRBY2Nlc3NHcmFudHMiLCJ2YWx1ZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhdGFzZXRzL0VHQUQwMDAwMTAwMjA4MCJ9LCJpc3MiOiJodHRwczovL2VnYS5lYmkuYWMudWs6ODA1My9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyLyIsImV4cCI6MTU5ODk1Mjg1MCwiaWF0IjoxNTkyODI0NTE0LCJqdGkiOiI0MmQ5NzIzZC00OTNmLTQ3NGEtOGU0Yi03ZmRkZGE1YzM5ZjEifQ.JkWP6emg3irl4-97ZfCvri-SOK4WRUqHncledf5yg14";
        String jwt2 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWlsaW8iLCJnYTRnaF92aXNhX3YxIjp7ImFzc2VydGVkIjoxNTY4ODE0MzgzLCJieSI6ImRhYyIsInNvdXJjZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhY3MvRUdBQzAwMDAxMDAwNTE0IiwidHlwZSI6IkNvbnRyb2xsZWRBY2Nlc3NHcmFudHMiLCJ2YWx1ZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhdGFzZXRzL0VHQUQwMDAwMTAwMjA5MCJ9LCJpc3MiOiJodHRwczovL2VnYS5lYmkuYWMudWs6ODA1My9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyLyIsImV4cCI6MTU5ODk1MjgzNCwiaWF0IjoxNTkyODI0NTE0LCJqdGkiOiI0MmQ5NzIzZC00OTNmLTQ3NGEtOGU0Yi03ZmRkZGE1YzM5ZjEifQ.58IQVBhhgOxvpiH_0VICoQsAO-BJdRtmSiUpalhb5Hw";

        ResponseEntity<JWTTokenResponse[]> result = this.restTemplate.postForEntity(uri, Arrays.asList(jwt1, jwt2),
                JWTTokenResponse[].class);
        JWTTokenResponse[] responses = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(responses).hasSize(2);

        assertThat(responses).filteredOn(e -> e.getStatus() == HttpStatus.CREATED.value()).hasSize(2);
    }

    @Test
    @DisplayName("OK Response when DELETE request sent to /jwt/{accountId}/permissions endpoint")
    public void shouldReturnOkWithNoResponseBody() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/jwt/EGAW0000004000/permissions";
        URI uri = new URI(baseUrl);

        String jwt1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWlsaW8iLCJnYTRnaF92aXNhX3YxIjp7ImFzc2VydGVkIjoxNTY4ODE0MzgzLCJieSI6ImRhYyIsInNvdXJjZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhY3MvRUdBQzAwMDAxMDAwNTE0IiwidHlwZSI6IkNvbnRyb2xsZWRBY2Nlc3NHcmFudHMiLCJ2YWx1ZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhdGFzZXRzL0VHQUQwMDAwMTAwMjA4MCJ9LCJpc3MiOiJodHRwczovL2VnYS5lYmkuYWMudWs6ODA1My9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyLyIsImV4cCI6MTU5ODk1Mjg1MCwiaWF0IjoxNTkyODI0NTE0LCJqdGkiOiI0MmQ5NzIzZC00OTNmLTQ3NGEtOGU0Yi03ZmRkZGE1YzM5ZjEifQ.JkWP6emg3irl4-97ZfCvri-SOK4WRUqHncledf5yg14";

        this.restTemplate.postForEntity(uri, Arrays.asList(jwt1), JWTTokenResponse[].class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("value", "https://ega-archive.org/datasets/EGAD00001002080");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("NOT_FOUND Response when DELETE request sent to /jwt/{accountId}/permissions endpoint")
    public void shouldReturnNotFoundForDeleteOperation() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/jwt/EGAW0000005000/permissions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("value", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}