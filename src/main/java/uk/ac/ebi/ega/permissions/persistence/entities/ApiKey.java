package uk.ac.ebi.ega.permissions.persistence.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@IdClass(ApiKeyId.class)
public class ApiKey {

    @Id
    private String username;

    @Id
    private String keyName;

    private Date expiration;
    private String reason;
    private String salt;
    private String privateKey;

    public ApiKey() {

    }

    public ApiKey(String username, String keyName, Date expiration, String reason, String salt, String privateKey) {
        this.username = username;
        this.keyName = keyName;
        this.expiration = expiration;
        this.reason = reason;
        this.salt = salt;
        this.privateKey = privateKey;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
