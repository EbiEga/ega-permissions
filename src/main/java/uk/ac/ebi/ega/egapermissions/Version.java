package uk.ac.ebi.ega.egapermissions;

public class Version {
    private final String version;

    public Version(String content) {
        this.version = content;
    }

    public String getVersion() {
        return version;
    }
}
