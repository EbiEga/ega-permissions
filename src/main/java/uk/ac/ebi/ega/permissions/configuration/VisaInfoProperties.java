package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ega-permissions.visainfo")
public class VisaInfoProperties {

    private String issuer;
    private long expireAfter;
    private int iat;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
    }

    public int getIat() {
        return iat;
    }

    public void setIat(int iat) {
        this.iat = iat;
    }
}
