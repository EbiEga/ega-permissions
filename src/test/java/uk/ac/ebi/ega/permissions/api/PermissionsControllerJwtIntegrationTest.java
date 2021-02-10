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
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "unsecuretest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "uk.ac.ebi.ega.permissions")
class PermissionsControllerJwtIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private PassportClaimRepository passportClaimRepository;

    @LocalServerPort
    int port;


    @BeforeEach
    public void setup() {
        Account account = new Account("EGAW0000001000", "Test", "Test", "test@ebi.ac.uk", "Active");
        accountRepository.save(account);

        UserGroup userGroup = new UserGroup("EGAW0000001000", "", GroupType.EGAAdmin, Permission.write);
        userGroupRepository.save(userGroup);

        passportClaimRepository.deleteAll();
    }

    @Test
    @DisplayName("NOT_FOUND Response when GET request sent to /permissions endpoint")
    public void shouldReturnNotFoundWithInvalidUserAccountId() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?format=JWT";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-account-id", "EGAW0000001000");

        HttpEntity<Object> entity = new HttpEntity(headers);
        ResponseEntity<Object> result = restTemplate.exchange(uri, HttpMethod.GET, entity, Object.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("OK Response when GET request sent to /permissions endpoint")
    public void shouldReturnOkWithUserPermissions() throws Exception {
        String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000002000&format=PLAIN";

        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setBy("dac");

        this.restTemplate.postForEntity(new URI(baseUrl), Arrays.asList(passportVisaObject), PermissionsResponse[].class);

        baseUrl = "http://localhost:" + port + "/permissions?format=JWT";

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-account-id", "EGAW0000002000");

        HttpEntity<Object> entity = new HttpEntity(headers);
        ResponseEntity<Object[]> result = restTemplate.exchange(new URI(baseUrl), HttpMethod.GET, entity, Object[].class);

        Object[] permissions = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(permissions).hasSize(1);
    }

    @Test
    @DisplayName("MULTI_STATUS Response when POST request sent to /permissions endpoint")
    public void shouldReturnMultiStatusWithResponses() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions?format=JWT";
        URI uri = new URI(baseUrl);

        String jwt1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWlsaW8iLCJnYTRnaF92aXNhX3YxIjp7ImFzc2VydGVkIjoxNTY4ODE0MzgzLCJieSI6ImRhYyIsInNvdXJjZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhY3MvRUdBQzAwMDAxMDAwNTE0IiwidHlwZSI6IkNvbnRyb2xsZWRBY2Nlc3NHcmFudHMiLCJ2YWx1ZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhdGFzZXRzL0VHQUQwMDAwMTAwMjA4MCJ9LCJpc3MiOiJodHRwczovL2VnYS5lYmkuYWMudWs6ODA1My9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyLyIsImV4cCI6MTU5ODk1Mjg1MCwiaWF0IjoxNTkyODI0NTE0LCJqdGkiOiI0MmQ5NzIzZC00OTNmLTQ3NGEtOGU0Yi03ZmRkZGE1YzM5ZjEifQ.JkWP6emg3irl4-97ZfCvri-SOK4WRUqHncledf5yg14";
        String jwt2 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWlsaW8iLCJnYTRnaF92aXNhX3YxIjp7ImFzc2VydGVkIjoxNTY4ODE0MzgzLCJieSI6ImRhYyIsInNvdXJjZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhY3MvRUdBQzAwMDAxMDAwNTE0IiwidHlwZSI6IkNvbnRyb2xsZWRBY2Nlc3NHcmFudHMiLCJ2YWx1ZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhdGFzZXRzL0VHQUQwMDAwMTAwMjA5MCJ9LCJpc3MiOiJodHRwczovL2VnYS5lYmkuYWMudWs6ODA1My9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyLyIsImV4cCI6MTU5ODk1MjgzNCwiaWF0IjoxNTkyODI0NTE0LCJqdGkiOiI0MmQ5NzIzZC00OTNmLTQ3NGEtOGU0Yi03ZmRkZGE1YzM5ZjEifQ.58IQVBhhgOxvpiH_0VICoQsAO-BJdRtmSiUpalhb5Hw";

        HashMap<String, String> object1 = new HashMap<>();
        object1.put("jwt", jwt1);
        object1.put("format", "JWT");

        HashMap<String, String> object2 = new HashMap<>();
        object2.put("jwt", jwt2);
        object2.put("format", "JWT");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-account-id", "EGAW0000003000");

        HttpEntity<Object[]> entities = new HttpEntity(Arrays.asList(object1, object2), headers);

        ResponseEntity<Object[]> result = this.restTemplate.postForEntity(uri, entities, Object[].class);
        Object[] responses = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("OK Response when DELETE request sent to /{accountId}/permissions endpoint")
    public void shouldReturnOkWithNoResponseBody() throws Exception {
        String baseUrl = "http://localhost:" + port + "/permissions?account-id=EGAW0000004000&format=JWT";

        String jwt1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWlsaW8iLCJnYTRnaF92aXNhX3YxIjp7ImFzc2VydGVkIjoxNTY4ODE0MzgzLCJieSI6ImRhYyIsInNvdXJjZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhY3MvRUdBQzAwMDAxMDAwNTE0IiwidHlwZSI6IkNvbnRyb2xsZWRBY2Nlc3NHcmFudHMiLCJ2YWx1ZSI6Imh0dHBzOi8vZWdhLWFyY2hpdmUub3JnL2RhdGFzZXRzL0VHQUQwMDAwMTAwMjA4MCJ9LCJpc3MiOiJodHRwczovL2VnYS5lYmkuYWMudWs6ODA1My9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyLyIsImV4cCI6MTU5ODk1Mjg1MCwiaWF0IjoxNTkyODI0NTE0LCJqdGkiOiI0MmQ5NzIzZC00OTNmLTQ3NGEtOGU0Yi03ZmRkZGE1YzM5ZjEifQ.JkWP6emg3irl4-97ZfCvri-SOK4WRUqHncledf5yg14";

        HashMap<String, String> object1 = new HashMap<>();
        object1.put("jwt", jwt1);
        object1.put("format", "JWT");

        this.restTemplate.postForEntity(new URI(baseUrl), Arrays.asList(object1), Object[].class);

        baseUrl = "http://localhost:" + port + "/permissions?format=JWT";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("x-account-id", "EGAW0000004000");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("values", "https://ega-archive.org/datasets/EGAD00001002080");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("NOT_FOUND Response when DELETE request sent to /permissions endpoint")
    public void shouldReturnNotFoundForDeleteOperation() throws Exception {
        final String baseUrl = "http://localhost:" + port + "/permissions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("x-account-id", "EGAW0000005000");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("values", "https://ega-archive.org/datasets/EGAD00002222222");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity result = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}