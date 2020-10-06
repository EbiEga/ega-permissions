package uk.ac.ebi.ega.permissions.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserGroupId implements Serializable {

    private String sourceAccountId;
    private String destinationAccountId;

    public UserGroupId() {

    }

    public UserGroupId(String sourceAccountId, String destinationAccountId) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroupId that = (UserGroupId) o;
        return Objects.equals(sourceAccountId, that.sourceAccountId) &&
                Objects.equals(destinationAccountId, that.destinationAccountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAccountId, destinationAccountId);
    }
}
