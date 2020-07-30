package uk.ac.ebi.ega.permissions.persistence.entities;

import java.util.List;

//TODO: This will become a JPA Entity Later
public class TokenPayload {

    private String sub;

    private String iss;

    private Integer iat;

    private String jti;

    private Integer exp;

    List<PassportClaim> claims;

    public TokenPayload(){

    }

    public TokenPayload(String sub, String iss, Integer iat, String jti, Integer exp, List<PassportClaim> claims) {
        this.sub = sub;
        this.iss = iss;
        this.iat = iat;
        this.jti = jti;
        this.exp = exp;
        this.claims = claims;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public Integer getIat() {
        return iat;
    }

    public void setIat(Integer iat) {
        this.iat = iat;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public List<PassportClaim> getClaims() {
        return claims;
    }

    public void setClaims(List<PassportClaim> claims) {
        this.claims = claims;
    }
}
