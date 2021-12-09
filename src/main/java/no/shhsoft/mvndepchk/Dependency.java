package no.shhsoft.mvndepchk;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class Dependency {

    private final String type;
    private final String groupId;
    private final String artifactId;
    private final Version version;

    public Dependency(final String type, final String groupId, final String artifactId, final Version version) {
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return type + ":" + groupId + "." + artifactId + "@" + version;
    }

}
