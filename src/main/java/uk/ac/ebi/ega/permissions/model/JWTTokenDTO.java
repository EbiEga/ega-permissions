package uk.ac.ebi.ega.permissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JWTTokenDTO {

    private List<String> userClaimsTokens;

    private JWTTokenDTO() {
    }

    public JWTTokenDTO(final List<String> userClaimsTokens) {
        this.userClaimsTokens = userClaimsTokens;
    }

    @JsonProperty("ga4gh_passport_v1")
    public List<String> getUserClaimsToken() {
        return userClaimsTokens;
    }
}
