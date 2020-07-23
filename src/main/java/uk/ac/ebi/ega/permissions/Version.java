package uk.ac.ebi.ega.permissions;

public class Version {
    private final String version;

    public Version(String content) {
        this.version = content;
    }

    public String getVersion() {
        return version;
    }
}
