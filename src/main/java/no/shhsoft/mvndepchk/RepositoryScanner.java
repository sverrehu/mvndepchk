package no.shhsoft.mvndepchk;

import no.shhsoft.utils.RegexCache;
import no.shhsoft.utils.RegexUtils;
import no.shhsoft.utils.StringUtils;
import no.shhsoft.web.utils.HttpFetcher;
import no.shhsoft.web.utils.UnexpectedHttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

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
            String content = StringUtils.newStringUtf8(HttpFetcher.get(urlToDependency));
            content = StringUtils.remove(content, '\n');
            final String[] matches = RegexCache.getMatchGroups(".*<a href=\"([0-9][^\"]*)/\".*", content);
            for (final String match : matches) {
                versions.add(Version.fromString(match));
            }
        } catch (final UnexpectedHttpStatusCodeException e) {
            if (e.getStatusCode() == 404) {
                return;
            }
            throw e;
        }
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
