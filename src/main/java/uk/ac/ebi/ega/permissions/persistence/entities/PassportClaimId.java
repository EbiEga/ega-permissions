package uk.ac.ebi.ega.permissions.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class PassportClaimId implements Serializable {

    private String accountId;
    private String value;

    public PassportClaimId(){
    }

    public PassportClaimId(String accountId, String value) {
        this.accountId = accountId;
        this.value = value;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassportClaimId that = (PassportClaimId) o;
        return accountId.equals(that.accountId) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, value);
    }
}
