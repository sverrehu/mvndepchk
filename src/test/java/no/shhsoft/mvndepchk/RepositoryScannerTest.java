package no.shhsoft.mvndepchk;

import no.shhsoft.utils.IoUtils;
import no.shhsoft.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class RepositoryScannerTest {

    @Test
    public void shouldParseFromCentral() {
        final String content = StringUtils.newStringUtf8(IoUtils.readResource("commons-codec.html"));
        final List<Version> allVersions = new RepositoryScanner(Collections.emptyList()).getAllVersions(content);
        Assert.assertEquals(4, allVersions.size());
        Assert.assertEquals(Version.fromString("1.0-dev"), allVersions.get(0));
        Assert.assertEquals(Version.fromString("1.1"), allVersions.get(1));
        Assert.assertEquals(Version.fromString("1.10"), allVersions.get(2));
        Assert.assertEquals(Version.fromString("20041127.091804"), allVersions.get(3));
    }
}
