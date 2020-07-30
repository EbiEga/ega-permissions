package uk.ac.ebi.ega.permissions.persistence.entities;

//TODO: This will become a JPA Entity Later
public class PassportClaim {

    private String type;

    private Integer asserted;

    private String value;

    private String source;

    private String by;

    public PassportClaim(){

    }

    public PassportClaim(String type, Integer asserted, String value, String source, String by) {
        this.type = type;
        this.asserted = asserted;
        this.value = value;
        this.source = source;
        this.by = by;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAsserted() {
        return asserted;
    }

    public void setAsserted(Integer asserted) {
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

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }
}
