package uk.ac.ebi.ega.permissions.mapper;/*
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

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.permissions.model.GroupUser;
import uk.ac.ebi.ega.permissions.model.GroupUserDTO;
import uk.ac.ebi.ega.permissions.model.PermissionLevel;
import uk.ac.ebi.ega.permissions.persistence.entities.Permission;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface GroupUserMapper {

    @Mapping(target = "userEmail", source = "email")
    GroupUser fromDTO(GroupUserDTO dto);

    List<GroupUser> fromDTOList(List<GroupUserDTO> dtoList);

    default PermissionLevel mapPermissionLevel(Permission permission) {
        switch (permission) {
            case read:
                return PermissionLevel.READ;
            case write:
                return PermissionLevel.WRITE;
            default:
                return null;
        }
    }

    default Permission mapPermission(PermissionLevel permissionLevel) {
        switch (permissionLevel) {
            case READ:
                return Permission.read;
            case WRITE:
                return Permission.write;
            default:
                return null;
        }
    }
}
