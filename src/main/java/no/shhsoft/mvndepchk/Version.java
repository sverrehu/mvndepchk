package no.shhsoft.mvndepchk;

import no.shhsoft.utils.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class Version {

    final String[] parts;
    final String compatibility;

    private Version(final String[] parts, final String compatibility) {
        this.parts = parts;
        this.compatibility = compatibility;
    }

    public static Version fromString(final String versionString) {
        String s = versionString;
        String compatibility = null;
        final int dashIndex = s.indexOf('-');
        if (dashIndex >= 0) {
            compatibility = s.substring(dashIndex + 1);
            s = s.substring(0, dashIndex);
        }
        final String[] parts = StringUtils.split(s, new char[] { '.' }, true, false);
        for (int q = 0; q < parts.length; q++) {
            final String part = parts[q];
            if (part.length() > 1 && part.toLowerCase(Locale.ROOT).charAt(0) == 'v' && StringUtils.isNumeric(part.substring(1))) {
                parts[q] = part.substring(1);
            }
        }
        return new Version(parts, compatibility);
    }

    public String[] getParts() {
        return parts;
    }

    public String getCompatibility() {
        return compatibility;
    }

    public boolean isCompatible(final Version other) {
        return compatibility == null && other.compatibility == null || compatibility != null && compatibility.equals(other.compatibility);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String part : parts) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(part);
        }
        if (compatibility != null) {
            sb.append('-');
            sb.append(compatibility);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Version version = (Version) o;
        return Arrays.equals(parts, version.parts) && Objects.equals(compatibility, version.compatibility);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(compatibility);
        result = 31 * result + Arrays.hashCode(parts);
        return result;
    }

}
