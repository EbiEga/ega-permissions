package uk.ac.ebi.ega.permissions.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class ApiKeyId implements Serializable {

    private String username;
    private String keyName;

    public ApiKeyId(){

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
