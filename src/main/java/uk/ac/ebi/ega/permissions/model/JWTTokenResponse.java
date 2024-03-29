package uk.ac.ebi.ega.permissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JWTTokenResponse {

    private String ga4ghVisaV1;
    private Integer status;
    private String message = null;

    private JWTTokenResponse() {
    }

    public JWTTokenResponse(String ga4ghVisaV1, Integer status, String message) {
        this.ga4ghVisaV1 = ga4ghVisaV1;
        this.status = status;
        this.message = message;
    }

    public JWTTokenResponse(final String ga4ghVisaV1) {
        this.ga4ghVisaV1 = ga4ghVisaV1;
    }

    @JsonProperty("ga4gh_visa_v1")
    public String getUserClaimToken() {
        return this.ga4ghVisaV1;
    }

    @JsonProperty("status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
