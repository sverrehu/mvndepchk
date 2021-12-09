package no.shhsoft.mvndepchk;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class VersionComparatorTest {

    @Test
    public void testMiscComparisons() {
        Assert.assertTrue(compare("1.0.0", "1.0.0") == 0);
        Assert.assertTrue(compare("1.0.0", "1.0.1") > 0);
        Assert.assertTrue(compare("1.0.1", "1.0.0") < 0);
        Assert.assertTrue(compare("1.1.0", "1.0.0") < 0);
        Assert.assertTrue(compare("1.0.0.1", "1.0.0") < 0);
        Assert.assertTrue(compare("1.0.0.1", "1.0.1") > 0);
        Assert.assertTrue(compare("1.0.v0", "1.0.0") == 0);
        Assert.assertTrue(compare("1.0.0-foo", "1.0.0") == 0);
        Assert.assertTrue(compare("1.0.0-foo", "1.0.0-bar") == 0);
        Assert.assertTrue(compare("1.9", "1.14") > 0);
    }

    private int compare(final String v1, final String v2) {
        return new VersionComparator().compare(Version.fromString(v1), Version.fromString(v2));
    }

}
