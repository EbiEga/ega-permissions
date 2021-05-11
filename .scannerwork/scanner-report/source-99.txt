/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.permissions.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class ApiKeyId implements Serializable {

    private String username;
    private String keyName;

    public ApiKeyId() {

    }

    public ApiKeyId(String username, String keyName) {
        this.username = username;
        this.keyName = keyName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKeyId apiKeyId = (ApiKeyId) o;
        return username.equals(apiKeyId.username) &&
                keyName.equals(apiKeyId.keyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, keyName);
    }
}
