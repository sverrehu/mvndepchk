package no.shhsoft.mvndepchk;

import no.shhsoft.utils.IoUtils;
import no.shhsoft.utils.StringUtils;
import no.shhsoft.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class MavenDependencyChecker {

    private void checkUpdates(final String pomFile) {
        final Document doc = XmlUtils.parse(IoUtils.readFile(pomFile));
        final Map<String, String> properties = getProperties(doc);
        final List<String> repositoryUrls = getRepositories(doc, properties);
        final List<Dependency> dependencies = getDependencies(doc, properties);
        final List<String> modules = getModules(doc, properties);
        final RepositoryScanner repositoryScanner = new RepositoryScanner(repositoryUrls);
        final StringBuilder sb = new StringBuilder();
        for (final Dependency dependency : dependencies) {
            final List<Version> availableVersions = repositoryScanner.getAvailableVersions(dependency);
            for (final Version version : availableVersions) {
                if (new VersionComparator().compare(dependency.getVersion(), version) <= 0) {
                    continue;
                }
                if (!dependency.getVersion().isCompatible(version)) {
                    continue;
                }
                sb.append("  ").append(dependency).append(" -> ").append(version).append("\r\n");
            }
        }
        if (sb.length() > 0) {
            System.out.println(pomFile);
            System.out.println(sb.toString());
        }
        for (final String module : modules) {
            final int lastSlashIndex = pomFile.lastIndexOf('/');
            final String dir = pomFile.substring(0, lastSlashIndex + 1);
            final String modulePomFile = dir + module + "/pom.xml";
            checkUpdates(modulePomFile);
        }
    }

    private List<Dependency> getDependencies(final Document doc, final Map<String, String> properties) {
        final List<Dependency> dependencies = new ArrayList<>();
        addDependencies(dependencies, doc, properties, "dependency");
        addDependencies(dependencies, doc, properties, "plugin");
        return dependencies;
    }

    private void addDependencies(final List<Dependency> dependencies, final Document doc, final Map<String, String> properties, final String tagName) {
        final NodeList dependencyNodes = doc.getElementsByTagName(tagName);
        for (int dependenciesIndex = 0; dependenciesIndex < dependencyNodes.getLength(); dependenciesIndex++) {
            final Node dependencyNode = dependencyNodes.item(dependenciesIndex);
            String groupId = null;
            String artifactId = null;
            Version version = null;
            for (final Element dependencyElement : getChildElements(dependencyNode)) {
                final String name = dependencyElement.getNodeName().trim();
                final String value = expandProperties(dependencyElement.getTextContent().trim(), properties);
                if ("groupId".equals(name)) {
                    groupId = value;
                } else if ("artifactId".equals(name)) {
                    artifactId = value;
                } else if ("version".equals(name)) {
                    version = Version.fromString(value);
                }
            }
            if (groupId != null && artifactId != null && version != null) {
                dependencies.add(new Dependency(tagName, groupId, artifactId, version));
            }
        }
    }

    private Map<String, String> getProperties(final Document doc) {
        final Map<String, String> properties = new HashMap<>();
        for (final Element propertiesElement : getTagElements(doc.getDocumentElement(), "properties")) {
            for (final Element propertyElement : getChildElements(propertiesElement)) {
                final String key = propertyElement.getNodeName().trim();
                final String value = propertyElement.getTextContent().trim();
                properties.put(key, value);
            }
        }
        return properties;
    }

    private List<String> getRepositories(final Document doc, final Map<String, String> properties) {
        final List<String> repositories = new ArrayList<>();
        final NodeList repositoryNodes = doc.getElementsByTagName("repository");
        for (int repositoriesIndex = 0; repositoriesIndex < repositoryNodes.getLength(); repositoriesIndex++) {
            final Element repositoryNode = (Element) repositoryNodes.item(repositoriesIndex);
            final String url = XmlUtils.findElement(repositoryNode, "url").getTextContent().trim();
            repositories.add(expandProperties(url, properties));
        }
        return repositories;
    }

    private List<String> getModules(final Document doc, final Map<String, String> properties) {
        final List<String> modules = new ArrayList<>();
        final NodeList moduleNodes = doc.getElementsByTagName("module");
        for (int moduleIndex = 0; moduleIndex < moduleNodes.getLength(); moduleIndex++) {
            final Element moduleNode = (Element) moduleNodes.item(moduleIndex);
            modules.add(expandProperties(moduleNode.getTextContent().trim(), properties));
        }
        return modules;
    }

    private String expandProperties(final String value, final Map<String, String> properties) {
        String s = value;
        for (final String property : properties.keySet()) {
            s = StringUtils.replace(s, "${" + property + "}", properties.get(property));
        }
        return s;
    }

    private List<Element> getChildElements(final Node root) {
        return getTagElements(root, null);
    }

    private List<Element> getTagElements(final Node root, final String name) {
        final List<Element> elements = new ArrayList<>();
        final NodeList childNodes = root.getChildNodes();
        for (int q = 0; q < childNodes.getLength(); q++) {
            final Node node = childNodes.item(q);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (name != null && !name.equals(node.getNodeName())) {
                continue;
            }
            elements.add((Element) node);
        }
        return elements;
    }

    public static void main(final String[] args) {
        if (args.length == 0) {
            System.out.println("mvndepchk path/to/pom.xml [...]");
            return;
        }
        for (final String arg : args) {
            new MavenDependencyChecker().checkUpdates(arg);
        }
    }

}
