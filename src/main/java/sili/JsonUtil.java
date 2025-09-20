package sili;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple JSON serializer/deserializer using a singleton mapper.
 */
public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static String toJson(Object o) {
        try { return MAPPER.writeValueAsString(o); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    public static <T> T fromJson(String s, Class<T> cls) {
        try { return MAPPER.readValue(s, cls); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
