package uk.ac.ebi.ega.permissions.api;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.TestApplication;
import uk.ac.ebi.ega.permissions.configuration.TestEntityManagerConfig;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import java.net.URI;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        TestApplication.class,
        TestEntityManagerConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PermissionsControllerPlainIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @LocalServerPort
    int port;

    @BeforeEach
    public void setup() {
        Account account = new Account("EGAW0000001000", "Test", "Test", "test@ebi.ac.uk", "Active");
        accountRepository.save(account);

        UserGroup userGroup = new UserGroup("EGAW0000001000", "", GroupType.EGAAdmin, Permission.write);
        userGroupRepository.save(userGroup);
    }


    @Test
    @DisplayName("NOT_FOUND Response when GET request sent to /{accountId}/permissions endpoint with format PLAIN")
    public void shouldReturnNotFoundWithInvalidUserAccountId() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/EGAW0000001000/permissions?format=PLAIN";
        URI uri = new URI(baseUrl);
        ResponseEntity result = this.restTemplate.getForEntity(uri, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /{accountId}/permissions endpoint with format PLAIN")
    public void shouldReturnOkWithUserPermissions() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/EGAW0000002000/permissions?format=PLAIN";
        URI uri = new URI(baseUrl);

        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setBy("dac");

        this.restTemplate.postForEntity(uri, Arrays.asList(passportVisaObject), PermissionsResponse[].class);

        ResponseEntity<Object[]> result = this.restTemplate.getForEntity(uri, Object[].class);
        Object[] permissions = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(1);
    }

    @Test
    @DisplayName("MULTI_STATUS Response when POST request sent to /plain/{accountId}/permissions endpoint with format PLAIN")
    public void shouldReturnMultiStatusWithResponses() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/EGAW0000003000/permissions?format=PLAIN";
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
    @DisplayName("OK Response when DELETE request sent to /{accountId}/permissions endpoint")
    public void shouldReturnOkWithNoResponseBody() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/EGAW0000004000/permissions?format=PLAIN";
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
    @DisplayName("NOT_FOUND Response when DELETE request sent to /plain/{accountId}/permissions endpoint with format PLAIN")
    public void shouldReturnNotFoundForDeleteOperation() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/EGAW0000005000/permissions?format=PLAIN";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("value", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /datasets/{datasetId}/users endpoint with format PLAIN")
    public void shouldReturnOkWithAccountUsers() throws Exception {
        String baseUrl = "http://localhost:" + port + "/EGAW0000004000/permissions?format=PLAIN";

        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setValue("EGAD00002222222");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setBy("dac");

        this.restTemplate.postForEntity(new URI(baseUrl), Arrays.asList(passportVisaObject), PermissionsResponse[].class);

        baseUrl = "http://localhost:" + port + "/datasets/EGAD00002222222/users";

        ResponseEntity<AccountAccess[]> result = this.restTemplate.getForEntity(new URI(baseUrl), AccountAccess[].class);
        AccountAccess[] accesses = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accesses).hasSize(1);
    }

}