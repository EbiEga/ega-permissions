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
package uk.ac.ebi.ega.permissions.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.ga4gh.jwt.passport.model.GroupUserDTO;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.Permission;
import uk.ac.ebi.ega.permissions.model.AccessGroup;
import uk.ac.ebi.ega.permissions.model.GroupUser;
import uk.ac.ebi.ega.permissions.model.PermissionLevel;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AccessGroupMapper {

    @Mapping(target = "userEmail", source = "email")
    GroupUser groupUserFromDTO(GroupUserDTO dto);

    @Mapping(target = "groupId", source = "accessGroupId.groupStableId")
    @Mapping(target = "description", ignore = true)
    AccessGroup accessGroupFromAccessGroupEntity(uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccessGroup accessGroup);

    List<GroupUser> groupUsersFromDTOs(List<GroupUserDTO> dtoList);

    List<AccessGroup> accessGroupsFromAccessGroupEntities(List<uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccessGroup> accessGroups);

    default PermissionLevel mapPermissionLevel(Permission permission) {
        switch (permission) {
            case READ:
                return PermissionLevel.READ;
            case WRITE:
                return PermissionLevel.WRITE;
            default:
                return null;
        }
    }

    default Permission mapPermission(PermissionLevel permissionLevel) {
        switch (permissionLevel) {
            case READ:
                return Permission.READ;
            case WRITE:
                return Permission.WRITE;
            default:
                return null;
        }
    }
}