package uk.ac.ebi.ega.permissions.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class AccessGroupId implements Serializable {

    private String egaAccountStableId;
    private String groupStableId;

    public AccessGroupId() {

    }

    public AccessGroupId(String egaAccountStableId, String groupStableId) {
        this.egaAccountStableId = egaAccountStableId;
        this.groupStableId = groupStableId;
    }

    public String getEgaAccountStableId() {
        return egaAccountStableId;
    }

    public void setEgaAccountStableId(String userId) {
        this.egaAccountStableId = userId;
    }

    public String getGroupStableId() {
        return groupStableId;
    }

    public void setGroupStableId(String groupId) {
        this.groupStableId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessGroupId that = (AccessGroupId) o;
        return Objects.equals(egaAccountStableId, that.egaAccountStableId) &&
                Objects.equals(groupStableId, that.groupStableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(egaAccountStableId, groupStableId);
    }
}
