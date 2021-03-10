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
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.helpers.AccessTokenHelper;
import uk.ac.ebi.ega.permissions.model.GroupUser;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionLevel;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessGroupsStepDefs {

    @Autowired
    World world;

    private RestTemplate restTemplate = new RestTemplate();
    private AccessTokenHelper accessTokenHelper;

    @When("^add user (.*?) to access group (.*?) with (.*?) permission$")
    public void addUserToAccessGroup(String userId, String groupId, String permission) throws URISyntaxException {

        PermissionLevel permissionLevel = permission.equals("write") ? PermissionLevel.WRITE : PermissionLevel.READ;
        GroupUser groupUser = new GroupUser().userAccountId(userId).permission(permissionLevel);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/access-groups/{groupId}/group-users")
                .buildAndExpand(groupId)
                .toUri();

        final HttpEntity<GroupUser> request = new HttpEntity<>(groupUser, this.world.getHeaders());
        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.POST, request, GroupUser.class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @When("^retrieve users for group (.*?)$")
    public void listGroupUsers(String groupId) throws URISyntaxException {

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI("http://localhost:8080"))
                .path("/access-groups/{groupId}/group-users")
                .buildAndExpand(groupId)
                .toUri();

        final HttpEntity request = new HttpEntity<>(this.world.getHeaders());
        try {
            //restTemplate is throwing an exception so we catch it to validate later as this scenario includes multiple response types
            world.response = restTemplate.exchange(requestURI, HttpMethod.GET, request, GroupUser[].class);
        } catch (HttpClientErrorException ex) {
            world.response = new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @And("response only contains group users")
    public void responseOnlyContainsItems(DataTable datasetsTable) {
        ResponseEntity<GroupUser[]> getResponse = (ResponseEntity<GroupUser[]>) world.response;
        List<String> expectedAccountIds = datasetsTable.transpose().asList(String.class);
        List<String> returnedAccountIds = Arrays.stream(getResponse.getBody()).map(GroupUser::getUserAccountId).collect(Collectors.toList());
        assertThat(returnedAccountIds).hasSameElementsAs(expectedAccountIds);
    }

    @And("^database contains access group (.*?) for user (.*?) with (.*?) permission$")
    public void databaseContainsAccessGroupEGACForUserEGAWWithWritePermission(String groupId, String userId, String permissionStr) {
        Permission permission = permissionStr.equalsIgnoreCase("write") ? Permission.write : Permission.read;
        assertThat(this.world.userGroupRepository.findAll()).filteredOn(ug -> ug.getGroupStableId().equals(groupId) && ug.getEgaAccountStableId().equals(userId) && ug.getPermission() == permission).hasSize(1);
    }
}
