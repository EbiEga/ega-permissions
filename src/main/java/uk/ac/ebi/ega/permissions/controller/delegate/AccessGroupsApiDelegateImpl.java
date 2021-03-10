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
import uk.ac.ebi.ega.permissions.api.AccessGroupsApiDelegate;
import uk.ac.ebi.ega.permissions.configuration.security.customauthorization.HasAdminPermissions;
import uk.ac.ebi.ega.permissions.mapper.GroupUserMapper;
import uk.ac.ebi.ega.permissions.model.GroupUser;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;

import java.util.List;

public class AccessGroupsApiDelegateImpl implements AccessGroupsApiDelegate {

    private UserGroupDataService userGroupDataService;
    private GroupUserMapper groupUserMapper;

    public AccessGroupsApiDelegateImpl(UserGroupDataService userGroupDataService,
                                       GroupUserMapper groupUserMapper) {
        this.userGroupDataService = userGroupDataService;
        this.groupUserMapper = groupUserMapper;
    }

    @Override
    @HasAdminPermissions
    public ResponseEntity<List<GroupUser>> getGroupUsers(String groupId) {
        return ResponseEntity.ok(this.groupUserMapper.fromDTOList(this.userGroupDataService.getGroupUsers(groupId)));
    }

    @Override
    @HasAdminPermissions
    public ResponseEntity<GroupUser> postAccessGroup(String groupId, GroupUser groupUser) {
        AccessGroup userGroup = new AccessGroup(groupUser.getUserAccountId(), groupId, GroupType.DAC, groupUserMapper.mapPermission(groupUser.getPermission()));
        this.userGroupDataService.save(userGroup);
        return ResponseEntity.ok(groupUser);
    }
}
