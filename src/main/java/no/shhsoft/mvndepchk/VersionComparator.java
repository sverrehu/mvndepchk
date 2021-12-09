package no.shhsoft.mvndepchk;

import no.shhsoft.utils.StringUtils;

import java.util.Comparator;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class VersionComparator
implements Comparator<Version> {

    @Override
    public int compare(final Version v1, final Version v2) {
        for (int q = 0; q < Math.max(v1.parts.length, v2.parts.length); q++) {
            final String part1 = q < v1.parts.length ? v1.parts[q] : null;
            final String part2 = q < v2.parts.length ? v2.parts[q] : null;
            final int diff = compareParts(part1, part2);
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    private int compareParts(final String part1, final String part2) {
        if (part1 != null && part2 != null) {
            if (StringUtils.isNumeric(part1) && StringUtils.isNumeric(part2)) {
                return Integer.parseInt(part2) - Integer.parseInt(part1);
            }
            return part1.compareTo(part2);
        } else if (part1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
