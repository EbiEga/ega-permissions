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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
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
import uk.ac.ebi.ega.permissions.helpers.DatasetHelper;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.Authority;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.VisaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class PermissionsMgmtStepDefs {

    @Autowired
    World world;

    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void cleanBeforeEachScenario() {
        this.world.cleanPermissions();
    }

    @Given("^dataset (.*?) belongs to DAC (.*?)$")
    public void datasetBelongsToDAC(String datasetId, String dacStableId) {
        this.world.datasetHelper.insert(datasetId, "Test Dataset", dacStableId);
    }

    @Given("^DAC Admin user (.*?) with email (.*?) and (.*?) access to (.*?) exist$")
    public void dacAdminUserWithEmailAndAccessExists(String dacUserAccountId, String email, String accessLevel, String dacStableId) {
        Account account = new Account(dacUserAccountId, "Test DACAdmin " + dacUserAccountId, "Test", email, "Active");
        this.world.accountRepository.save(account);

        Permission permission = accessLevel.equals("write") ? Permission.write : Permission.read;
        UserGroup userGroup = new UserGroup(dacUserAccountId, dacStableId, GroupType.DAC, permission);
        this.world.userGroupRepository.save(userGroup);
    }

    @When("^user account (.*?) grants permissions to account (.*?) on dataset (.*?)$")
    public void userGrantsPermissionsToAccountOnDataset(String dacUserAccountId, String egaUserAccountId, String datasetId) throws URISyntaxException {
        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList(datasetId), dacUserAccountId);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/permissions")
                .query("account-id=" + egaUserAccountId + "&format=PLAIN")
                .build()
                .toUri();

        final HttpEntity<List<PassportVisaObject>> request = new HttpEntity<>(passportVisaObjects, getHeaders());
        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.POST, request, PermissionsResponse[].class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }

    }

    @Then("^dataset has status code (.*?)$")
    public void theResponseStatusIsResponse_statusAndTheDatasetStatusIsDataset_status(int datasetStatus) {
        if (datasetStatus != 0) {
            ResponseEntity<PermissionsResponse[]> postResponse = (ResponseEntity<PermissionsResponse[]>) world.response;
            assertThat(postResponse.getBody()[0].getStatus()).isEqualTo(datasetStatus);
        }
    }

    @And("^account (.*?) is an EGA Admin$")
    public void accountIsAnEGAAdmin(String egaAdminAccountId) {
        UserGroup userGroup = new UserGroup(egaAdminAccountId, "", GroupType.EGAAdmin, Permission.write);
        this.world.userGroupRepository.save(userGroup);
    }

    @And("^datasets belong to DAC (.*?)$")
    public void datasetsBelongToDAC(String dacStableId, DataTable datasetsTable) {
        List<String> datasets = datasetsTable.transpose().asList(String.class);
        datasets.forEach(dataset -> this.world.datasetHelper.insert(dataset, "Test Dataset", dacStableId));
    }

    @And("^user (.*?) has access to datasets$")
    public void userHasAccessToDatasets(String egaUserAccountId, DataTable datasetsTable) {
        List<String> datasets = datasetsTable.transpose().asList(String.class);
        List<PassportClaim> passportClaims = datasets.stream().map(dataset -> createPassportClaim(egaUserAccountId, dataset)).collect(Collectors.toList());
        this.world.passportClaimRepository.saveAll(passportClaims);
    }

    @When("^list permissions for account (.*?)$")
    public void userAccountEGAWListPermissionsForAccountEGAW(String egaUserAccountId) throws URISyntaxException {
        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/permissions")
                .query("account-id=" + egaUserAccountId + "&format=PLAIN")
                .build()
                .toUri();

        HttpEntity request = new HttpEntity(getHeaders());
        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.GET, request, Visa[].class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @When("^user lists permissions for himself$")
    public void userListPermissionsForHimself() throws URISyntaxException {
        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/me/permissions")
                .query("format=PLAIN")
                .build()
                .toUri();

        HttpEntity request = new HttpEntity(getHeaders());
        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.GET, request, Visa[].class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }
    }


    private List<PassportVisaObject> createPassportVisaObjects(List<String> values, String dacStableId) {
        List<PassportVisaObject> passportVisaObjects = new ArrayList<>(values.size());

        values.forEach(value -> {
            PassportVisaObject passportVisaObject = new PassportVisaObject();
            passportVisaObject.setSource(dacStableId);
            passportVisaObject.setType("ControlledAccessGrants");
            passportVisaObject.setValue(value);
            passportVisaObject.setAsserted(new Date().getTime());
            passportVisaObject.setBy("dac");
            passportVisaObjects.add(passportVisaObject);
        });
        return passportVisaObjects;
    }

    private PassportClaim createPassportClaim(String accountId, String value) {
        return new PassportClaim(accountId, VisaType.ControlledAccessGrants, new Date().getTime(), value, "SampleDAC", Authority.dac);
    }

    @And("response only contains items")
    public void responseOnlyContainsItems(DataTable datasetsTable) {
        ResponseEntity<Visa[]> getResponse = (ResponseEntity<Visa[]>) world.response;
        List<String> expectedDatasets = datasetsTable.transpose().asList(String.class);
        List<String> returnedDatasets = Arrays.stream(getResponse.getBody()).map(Visa::getGa4ghVisaV1).map(PassportVisaObject::getValue).collect(Collectors.toList());
        assertThat(returnedDatasets).hasSameElementsAs(expectedDatasets);
    }

    @And("response does not contain any items")
    public void responseShouldNotContainAnyItems() {
        ResponseEntity<Visa[]> getResponse = (ResponseEntity<Visa[]>) world.response;
        assertThat(getResponse.getBody()).isEmpty();
    }

    @And("^database still contains permissions for account (.*?)$")
    public void databaseShouldContainPermissionsForAccount(String accountId, DataTable datasetsTable) {
        List<String> datasets = datasetsTable.transpose().asList(String.class);
        List<String> grantedDatasets = this.world.passportClaimRepository.findAllByAccountId(accountId).stream().map(PassportClaim::getValue).collect(Collectors.toList());
        assertThat(grantedDatasets).hasSameElementsAs(datasets);
    }

    @When("^admin revokes (.*?) from user account (.*?)$")
    public void adminAccountRevokesPermissionFromUserAccount(String permission, String userAccountId) throws URISyntaxException {
        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/permissions")
                .query("account-id=" + userAccountId + "&values=" + permission)
                .build()
                .toUri();

        HttpEntity request = new HttpEntity(getHeaders());

        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.DELETE, request, Object.class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(this.world.bearerAccessToken);
        return headers;
    }
}
