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
package uk.ac.ebi.ega.permissions.model;

import uk.ac.ebi.ega.permissions.persistence.entities.Permission;

public class GroupUserDTO {

    private String userAccountId;
    private String email;
    private Permission permission;

    private GroupUserDTO() {

    }

    public GroupUserDTO(String userAccountId, String email, Permission permission) {
        this.userAccountId = userAccountId;
        this.email = email;
        this.permission = permission;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getEmail() {
        return email;
    }

    public Permission getPermission() {
        return permission;
    }
    
}
