package no.shhsoft.mvndepchk;

import no.shhsoft.utils.StringUtils;
import no.shhsoft.utils.cache.TimeoutCache;
import no.shhsoft.web.utils.HttpFetcher;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class CachingHttpFetcher {

    private static final TimeoutCache<String, String> CACHE = new TimeoutCache<>();

    public static String get(final String url) {
        synchronized (CACHE) {
            String contents = CACHE.get(url);
            if (contents == null) {
                contents = StringUtils.newStringUtf8(HttpFetcher.get(url));
                CACHE.put(url, contents, 10 * 60 * 1000L);
            }
            return contents;
        }
    }

}
