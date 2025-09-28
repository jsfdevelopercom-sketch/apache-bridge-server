package sili;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses query parameters from a URI.
 */
public class QueryUtil {
    public static Map<String,String> parse(String uri) {
        Map<String,String> map = new HashMap<>();
        try {
            URI u = URI.create(uri);
            String query = u.getRawQuery();
            if (query == null) return map;
            for (String p : query.split("&")) {
                if (p.isEmpty()) continue;
                String[] kv = p.split("=", 2);
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name()) : "";
                map.put(k, v);
            }
        } catch (Exception ignore) {}
        return map;
    }
}
