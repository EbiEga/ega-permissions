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

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ega.permissions.helpers.AccessTokenHelper;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonStepDefs {

    @Autowired
    World world;

    private RestTemplate restTemplate = new RestTemplate();
    private AccessTokenHelper accessTokenHelper;

    @Given("^user (.*?) with email (.*?) exist$")
    public void userExistsWithEmail(String userAccountId, String email) {
        Account account = new Account(userAccountId, "Test Account " + userAccountId, "Test", email, "Active");
        this.world.accountRepository.save(account);
    }

    @Given("^user acquires a valid token$")
    public void userAcquiresValidToken() throws URISyntaxException {
        this.accessTokenHelper = new AccessTokenHelper(this.restTemplate, this.world.tokenParams);
        this.world.bearerAccessToken = this.accessTokenHelper.obtainAccessTokenFromEGA();
    }

    @Given("^user has an invalid token$")
    public void userHasAnInvalidToken() {
        this.world.bearerAccessToken = "invalid_token";
    }

    @Then("^response has status code (.*?)$")
    public void responseHasStatusCode(int expectedStatusCode) {
        assertThat(world.response.getStatusCodeValue()).isEqualTo(expectedStatusCode);
    }

}
