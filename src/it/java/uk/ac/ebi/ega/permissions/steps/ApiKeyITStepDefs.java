/*
 * Copyright 2021-2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.permissions.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class ApiKeyITStepDefs {

    @Autowired
    private World world;

    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void cleanBeforeEachScenario() {
        this.world.cleanApiKeys();
    }

    @When("^user request API_KEY Token (.*?)$")
    public void userRequestTokenWithKey(String tokenKey) throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(this.world.bearerAccessToken);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/api_key/generate")
                .query("id=" + tokenKey + "&expiration_date=2050-12-31&reason=test")
                .build()
                .toUri();

        HttpEntity request = new HttpEntity(headers);

        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.GET, request, CreatedAPIKey.class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @Then("^response contains token (.*?)$")
    public void responseContainsToken(String tokenKey) {
        ResponseEntity<CreatedAPIKey> createdAPIKeyResponse = (ResponseEntity<CreatedAPIKey>) world.response;
        assertThat(createdAPIKeyResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(createdAPIKeyResponse.getBody().getId()).isEqualTo(tokenKey);
        assertThat(createdAPIKeyResponse.getBody().getToken()).isNotEmpty();
    }
}
