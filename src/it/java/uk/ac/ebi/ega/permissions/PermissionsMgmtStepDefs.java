package uk.ac.ebi.ega.permissions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.helpers.AccessTokenHelper;
import uk.ac.ebi.ega.permissions.helpers.DatasetHelper;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class PermissionsMgmtStepDefs extends ITContextConfiguration {

    private RestTemplate restTemplate = new RestTemplate();
    private AccessTokenHelper accessTokenHelper;
    private DatasetHelper datasetHelper;

    private String accessToken;
    private ResponseEntity<PermissionsResponse[]> postResponse;
    private ResponseEntity<Visa[]> getResponse;

    @Given("^user account (.*?) with email (.*?) exist$")
    public void user_account_exist(String userAccountId, String email) {
        Account account = new Account(userAccountId, "Test Account " + userAccountId, "Test", email, "Active");
        this.accountRepository.save(account);
    }

    @Given("^dataset (.*?) belongs to DAC (.*?)$")
    public void dataset_belongs_to_dac(String datasetId, String dacStableId) {
        this.datasetHelper = new DatasetHelper(this.entityManagerFactory.createEntityManager());
        this.datasetHelper.insertDataset(datasetId, "Test Dataset", dacStableId);
    }

    @Given("^account (.*?) is linked to DAC (.*?) with (.*?) permissions$")
    public void account_is_linked_to_dac_with_some_permissions(String dacUserAccountId, String dacStableId, String accessLevel) {
        Permission permission = accessLevel.equals("write") ? Permission.write : Permission.read;
        UserGroup userGroup = new UserGroup(dacUserAccountId, dacStableId, GroupType.DAC, permission);
        this.userGroupRepository.save(userGroup);
    }

    @Given("^user account (.*?) has a valid token$")
    public void user_account_has_a_valid_token(String dacUserAccountId) throws URISyntaxException {
        this.accessTokenHelper = new AccessTokenHelper(this.restTemplate, this.tokenParams);
        this.accessToken = this.accessTokenHelper.obtainAccessTokenFromEGA();
    }

    @When("^user account (.*?) grants permissions to account (.*?) on dataset (.*?)$")
    public void user_account_grants_permissions_to_account_on_dataset(String dacUserAccountId, String egaUserAccountId, String datasetId) throws URISyntaxException {
        List<PassportVisaObject> passportVisaObjects = createPassportVisaObjects(Arrays.asList(datasetId), dacUserAccountId);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/permissions")
                .query("account-id=" + egaUserAccountId + "&format=PLAIN")
                .build()
                .toUri();

        final HttpEntity<List<PassportVisaObject>> request = new HttpEntity<>(passportVisaObjects, headers);
        this.postResponse = restTemplate.postForEntity(requestURI, request, PermissionsResponse[].class);
    }

    @Then("^the response status is (.*?) and the dataset status is (.*?)$")
    public void theResponseStatusIsResponse_statusAndTheDatasetStatusIsDataset_status(int responseStatus, int datasetStatus) {
        assertThat(this.postResponse.getStatusCodeValue()).isEqualTo(responseStatus);
        assertThat(this.postResponse.getBody()[0].getStatus()).isEqualTo(datasetStatus);
    }

    @And("^account (.*?) is an EGA Admin$")
    public void accountIsAnEGAAdmin(String egaAdminAccountId) {
        UserGroup userGroup = new UserGroup(egaAdminAccountId, "", GroupType.EGAAdmin, Permission.write);
        this.userGroupRepository.save(userGroup);
    }

    @And("^datasets belongs to DAC (.*?)$")
    public void datasetsBelongsToDACEGAC(String dacStableId, DataTable datasetsTable) {
        List<String> datasets = datasetsTable.transpose().asList(String.class);
        this.datasetHelper = new DatasetHelper(this.entityManagerFactory.createEntityManager());
        datasets.forEach(dataset -> this.datasetHelper.insertDataset(dataset, "Test Dataset", dacStableId));
    }

    @And("^user account (.*?) has permissions to datasets$")
    public void userAccountEGAWHasPermissionsToDatasets(String egaUserAccountId, DataTable datasetsTable) {
        List<String> datasets = datasetsTable.transpose().asList(String.class);
        List<PassportClaim> passportClaims = datasets.stream().map(dataset -> createPassportClaim(egaUserAccountId, dataset)).collect(Collectors.toList());
        this.passportClaimRepository.saveAll(passportClaims);
    }

    @When("^user account (.*?) list permissions for account (.*?)$")
    public void userAccountEGAWListPermissionsForAccountEGAW(String dacUserAccountId, String egaUserAccountId) throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/permissions")
                .query("account-id=" + egaUserAccountId + "&format=PLAIN")
                .build()
                .toUri();

        HttpEntity request = new HttpEntity(headers);
        this.getResponse = restTemplate.exchange(requestURI, HttpMethod.GET, request, Visa[].class);
    }

    @Then("^response should have status code (.*?) and only contain$")
    public void responseShouldHaveStatusCodeAndOnlyContain(int expectedStatusCode, DataTable datasetsTable) {
        List<String> expectedDatasets = datasetsTable.transpose().asList(String.class);
        List<String> returnedDatasets = Arrays.stream(this.getResponse.getBody()).map(Visa::getGa4ghVisaV1).map(PassportVisaObject::getValue).collect(Collectors.toList());

        assertThat(this.getResponse.getStatusCodeValue()).isEqualTo(expectedStatusCode);
        assertThat(returnedDatasets).hasSameElementsAs(expectedDatasets);
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

}
