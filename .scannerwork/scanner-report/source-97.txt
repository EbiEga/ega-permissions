package uk.ac.ebi.ega.permissions.persistence.entities;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(PassportClaimId.class)
@TypeDef(name = "visa_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "visa_authority", typeClass = PostgreSQLEnumType.class)
public class PassportClaim {
    @Id
    private String accountId;

    @Id
    private String value;

    @Enumerated(EnumType.STRING)
    @Type(type = "visa_type")
    private VisaType type;

    private Long asserted;

    private String source;

    @Enumerated(EnumType.STRING)
    @Type(type = "visa_authority")
    private Authority by;

    private String status = "approved";

    public PassportClaim() {

    }

    public PassportClaim(String accountId, VisaType type, Long asserted, String value, String source, Authority by) {
        this.accountId = accountId;
        this.type = type;
        this.asserted = asserted;
        this.value = value;
        this.source = source;
        this.by = by;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public VisaType getType() {
        return type;
    }

    public void setType(VisaType type) {
        this.type = type;
    }

    public Long getAsserted() {
        return asserted;
    }

    public void setAsserted(Long asserted) {
        this.asserted = asserted;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Authority getBy() {
        return by;
    }

    public void setBy(Authority by) {
        this.by = by;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
