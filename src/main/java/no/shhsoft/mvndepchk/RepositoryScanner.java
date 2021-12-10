package no.shhsoft.mvndepchk;

import no.shhsoft.utils.StringUtils;
import no.shhsoft.web.utils.HttpFetcher;
import no.shhsoft.web.utils.UnexpectedHttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class RepositoryScanner {

    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";
    private final List<String> repositoryUrls = new ArrayList<>();

    public RepositoryScanner(final List<String> additionalRepositoryUrls) {
        repositoryUrls.addAll(additionalRepositoryUrls);
        repositoryUrls.add(MAVEN_CENTRAL_URL);
    }

    public List<Version> getAvailableVersions(final Dependency dependency) {
        final List<Version> versions = new ArrayList<>();
        for (final String repositoryUrl : repositoryUrls) {
            addVersions(versions, dependency, repositoryUrl);
        }
        return versions;
    }

    private void addVersions(final List<Version> versions, final Dependency dependency, final String repositoryUrl) {
        final String urlToDependency = getUrlToDependency(dependency, repositoryUrl);
        try {
            String content = CachingHttpFetcher.get(urlToDependency);
            final List<Version> newVersions = getAllVersions(content);
            for (final Version version : newVersions) {
                if (!skipVersion(dependency, version)) {
                    versions.add(version);
                }
            }
        } catch (final UnexpectedHttpStatusCodeException e) {
            if (e.getStatusCode() == 404) {
                return;
            }
            throw e;
        }
    }

    List<Version> getAllVersions(final String content) {
        final List<Version> allVersions = new ArrayList<>();
        final Matcher matcher = Pattern.compile("<a href=\"([0-9][^\"]*)/\"").matcher(content);
        while (matcher.find()) {
            allVersions.add(Version.fromString(matcher.group(1)));
        }
        return allVersions;
    }

    private boolean skipVersion(final Dependency dependency, final Version version) {
        if (version.getParts().length == 0) {
            return true;
        }
        final String groupId = dependency.getGroupId();
        final String artifactId = dependency.getArtifactId();
        if ("commons-codec".equals(groupId) && "commons-codec".equals(artifactId)
            || "commons-httpclient".equals(groupId) && "commons-httpclient".equals(artifactId)
            || "antlr".equals(groupId) && "antlr".equals(artifactId)
            || "asm".equals(groupId)
        ) {
            return new VersionComparator().compare(version, Version.fromString("2000")) < 0;
        }
        return false;
    }

    private String getUrlToDependency(final Dependency dependency, final String repositoryUrl) {
        final StringBuilder sb = new StringBuilder();
        sb.append(repositoryUrl);
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        sb.append(dependency.getGroupId().replace('.', '/'));
        sb.append('/');
        sb.append(dependency.getArtifactId());
        sb.append('/');
        return sb.toString();
    }

}
