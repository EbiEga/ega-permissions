package uk.ac.ebi.ega.permissions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.helpers.AccessTokenHelper;
import uk.ac.ebi.ega.permissions.helpers.DatasetHelper;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class PermissionsMgmtStepDefs extends ITContextConfiguration {

    private RestTemplate restTemplate = new RestTemplate();
    private AccessTokenHelper accessTokenHelper;
    private DatasetHelper datasetHelper;

    private String accessToken;
    private ResponseEntity<PermissionsResponse[]> postPermissionsResponse;

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

        final HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(APPLICATION_JSON);
        tokenHeaders.setBearerAuth(this.accessToken);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/permissions")
                .query("account-id=" + egaUserAccountId + "&format=PLAIN")
                .build()
                .toUri();

        final HttpEntity<List<PassportVisaObject>> request = new HttpEntity<>(passportVisaObjects, tokenHeaders);
        this.postPermissionsResponse = restTemplate.postForEntity(requestURI, request, PermissionsResponse[].class);

    }

    @Then("^the response status is (.*?) and the dataset status is (.*?)$")
    public void theResponseStatusIsResponse_statusAndTheDatasetStatusIsDataset_status(int responseStatus, int datasetStatus) {
        assertThat(postPermissionsResponse.getStatusCodeValue()).isEqualTo(responseStatus);
        assertThat(postPermissionsResponse.getBody()[0].getStatus()).isEqualTo(datasetStatus);
    }

    @And("^account (.*?) is an EGA Admin$")
    public void accountIsAnEGAAdmin(String egaAdminAccountId) {
        UserGroup userGroup = new UserGroup(egaAdminAccountId, "", GroupType.EGAAdmin, Permission.write);
        this.userGroupRepository.save(userGroup);
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


}
