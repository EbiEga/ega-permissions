package uk.ac.ebi.ega.permissions.model;

import java.util.Date;

public class ApiKeyParams {

    private String username;
    private String keyId;
    private Date expiration;
    private String reason;
    private String privateKey;
    private String salt;
    private String token;

    public ApiKeyParams(String username, String keyId, Date expiration, String reason) {
        this.username = username;
        this.keyId = keyId;
        this.expiration = expiration;
        this.reason = reason;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
