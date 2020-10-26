package uk.ac.ebi.ega.permissions.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AccountElixirId {
    @Id
    private String accountId;
    private String elixirId;
    private String elixirEmail;

    public AccountElixirId(){
    }

    public AccountElixirId(String accountId, String elixirId, String elixirEmail) {
        this.accountId = accountId;
        this.elixirId = elixirId;
        this.elixirEmail = elixirEmail;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getElixirId() {
        return elixirId;
    }

    public void setElixirId(String elixirId) {
        this.elixirId = elixirId;
    }

    public String getElixirEmail() {
        return elixirEmail;
    }

    public void setElixirEmail(String elixirEmail) {
        this.elixirEmail = elixirEmail;
    }
}
