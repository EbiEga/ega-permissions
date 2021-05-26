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
package uk.ac.ebi.ega.permissions.controller.delegate;


import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.GroupUsersApiDelegate;
import uk.ac.ebi.ega.permissions.configuration.security.customauthorization.HasAdminPermissions;
import uk.ac.ebi.ega.permissions.mapper.AccessGroupMapper;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.service.AccessGroupDataService;

import java.util.List;
import java.util.stream.Collectors;

public class GroupUsersApiDelegateImpl implements GroupUsersApiDelegate {

    private AccessGroupDataService userGroupDataService;
    private AccessGroupMapper accessGroupMapper;

    public GroupUsersApiDelegateImpl(AccessGroupDataService userGroupDataService,
                                     AccessGroupMapper accessGroupMapper) {
        this.userGroupDataService = userGroupDataService;
        this.accessGroupMapper = accessGroupMapper;
    }

    @HasAdminPermissions
    @Override
    public ResponseEntity<Void> delUserFromGroup(String accountId, List<String> groupIds) {
        if (groupIds.contains("all")) {
            groupIds = this.userGroupDataService.getPermissionGroups(accountId).stream().map(ag -> ag.getAccessGroupId().getGroupStableId()).collect(Collectors.toList());
        }
        groupIds.forEach(groupId -> this.userGroupDataService.removeAccessGroup(accountId, groupId));
        return ResponseEntity.ok().build();
    }

    @Override
    @HasAdminPermissions
    public ResponseEntity<List<uk.ac.ebi.ega.permissions.model.AccessGroup>> getGroupsForUser(String accountId) {
        return ResponseEntity.ok(this.accessGroupMapper.accessGroupsFromAccessGroupEntities(this.userGroupDataService.getPermissionGroups(accountId)));
    }
}
