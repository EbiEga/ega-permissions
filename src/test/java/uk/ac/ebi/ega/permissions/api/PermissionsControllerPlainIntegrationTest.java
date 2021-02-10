/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "unsecuretest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "uk.ac.ebi.ega.permissions")
class PermissionsControllerPlainIntegrationTest {

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

        UserGroup userGroup1 = new UserGroup("EGAW0000001000", "", GroupType.EGAAdmin, Permission.write);
        UserGroup userGroup2 = new UserGroup("EGAW0000001000", "EGAC0000001000", GroupType.DAC, Permission.write);
        userGroupRepository.save(userGroup1);
        userGroupRepository.save(userGroup2);
    }


    @Test
    @DisplayName("NOT_FOUND Response when GET request sent to /permissions endpoint with format PLAIN")
    public void shouldReturnNotFoundWithInvalidUserAccountId() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000001000&format=PLAIN";
        URI uri = new URI(baseUrl);
        ResponseEntity result = this.restTemplate.getForEntity(uri, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /permissions endpoint with format PLAIN")
    public void shouldReturnOkWithUserPermissions() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000002000&format=PLAIN";
        URI uri = new URI(baseUrl);

        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("https://ega-archive.org/datasets/EGAD00002222222"));

        this.restTemplate.postForEntity(uri, passportVisaObjects, PermissionsResponse[].class);

        ResponseEntity<Object[]> result = this.restTemplate.getForEntity(uri, Object[].class);
        Object[] permissions = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(1);
    }

    @Test
    @DisplayName("MULTI_STATUS Response when POST request sent to /{accountId}/permissions endpoint with format PLAIN")
    public void shouldReturnMultiStatusWithResponses() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000003000&format=PLAIN";
        URI uri = new URI(baseUrl);

        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("https://ega-archive.org/datasets/EGAD00002222222",
                "https://ega-archive.org/datasets/EGAD00003333333"));

        ResponseEntity<PermissionsResponse[]> result = this.restTemplate.postForEntity(uri, passportVisaObjects, PermissionsResponse[].class);
        PermissionsResponse[] responses = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(responses).hasSize(2);

        assertThat(responses).filteredOn(e -> e.getStatus() == HttpStatus.CREATED.value()).hasSize(2);
    }

    @Test
    @DisplayName("OK Response when DELETE request sent to /permissions endpoint")
    public void shouldReturnOkWithNoResponseBody() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000004000&format=PLAIN";
        URI uri = new URI(baseUrl);

        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("https://ega-archive.org/datasets/EGAD00002222222"));

        this.restTemplate.postForEntity(uri, passportVisaObjects, PermissionsResponse[].class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("values", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("OK Response when DELETE ALL request sent to /permissions endpoint")
    @Sql(scripts = {"classpath:scripts/add-datasets.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void shouldReturnOkWithNoResponseBodyForDeleteAll() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000004000&format=PLAIN";
        URI uri = new URI(baseUrl);

        // Create 2 permissions using rest service
        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("EGAD00002222222", "EGAD00003333333"));

        this.restTemplate.postForEntity(uri, passportVisaObjects, PermissionsResponse[].class);

        ResponseEntity<Object[]> result = this.restTemplate.getForEntity(uri, Object[].class);
        Object[] permissions = result.getBody();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(2);

        // Delete 2 permissions using all parameter

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).queryParam("values", "all");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result1 = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the user no longer has active permissions (hence returning not found)

        result1 = this.restTemplate.getForEntity(uri, Object.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when DELETE MULTIPLE request sent to /permissions endpoint")
    @Sql(scripts = {"classpath:scripts/add-datasets.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void shouldReturnOkWithNoResponseBodyForDeleteMultiple() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000004000&format=PLAIN";
        URI uri = new URI(baseUrl);

        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("EGAD00002222222", "EGAD00003333333"));

        this.restTemplate.postForEntity(uri, passportVisaObjects, PermissionsResponse[].class);

        ResponseEntity<Object[]> result = this.restTemplate.getForEntity(uri, Object[].class);
        Object[] permissions = result.getBody();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(2);

        // Delete 2 permissions using all parameter

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).queryParam("values", "EGAD00002222222,EGAD00003333333");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result1 = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the user no longer has active permissions (hence returning not found)

        result1 = this.restTemplate.getForEntity(uri, Object.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("NOT_FOUND Response when DELETE request sent to /permissions endpoint with format PLAIN")
    public void shouldReturnNotFoundForDeleteOperation() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000005000&format=PLAIN";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("values", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /datasets/{datasetId}/users endpoint with format PLAIN")
    public void shouldReturnOkWithAccountUsers() throws Exception {
        String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000004000&format=PLAIN";

        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("EGAD00002222222"));

        this.restTemplate.postForEntity(new URI(baseUrl), passportVisaObjects, PermissionsResponse[].class);

        baseUrl = "http://localhost:" + port + "/datasets/EGAD00002222222/users";

        ResponseEntity<AccountAccess[]> result = this.restTemplate.getForEntity(new URI(baseUrl), AccountAccess[].class);
        AccountAccess[] accesses = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accesses).hasSize(1);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /me/permissions endpoint with format PLAIN")
    public void shouldReturnOkWithCurrentUserPermissions() throws Exception {
        String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000001000&format=PLAIN";

        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList("https://ega-archive.org/datasets/EGAD00002222222"));

        this.restTemplate.postForEntity(new URI(baseUrl), passportVisaObjects, PermissionsResponse[].class);

        baseUrl = "http://localhost:" + port + "/me/permissions?format=PLAIN";

        ResponseEntity<Object[]> result = this.restTemplate.getForEntity(new URI(baseUrl), Object[].class);
        Object[] permissions = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(1);
    }


    private List<PassportVisaObject> createPassportVisaObjects(List<String> values) {
        List<PassportVisaObject> passportVisaObjects = new ArrayList<>(values.size());

        values.forEach(value -> {
            PassportVisaObject passportVisaObject = new PassportVisaObject();
            passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
            passportVisaObject.setType("ControlledAccessGrants");
            passportVisaObject.setValue(value);
            passportVisaObject.setAsserted(1568814383L);
            passportVisaObject.setBy("dac");
            passportVisaObjects.add(passportVisaObject);
        });
        return passportVisaObjects;
    }
}