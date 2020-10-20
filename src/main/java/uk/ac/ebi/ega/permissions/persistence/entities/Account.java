package uk.ac.ebi.ega.permissions.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Account {
    @Id
    private String account_id;
    private String first_name;
    private String last_name;
    private String email;
    private String status;

    public Account() {
    }

    public Account(String account_id, String first_name, String last_name, String email, String status) {
        this.account_id = account_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.status = status;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
