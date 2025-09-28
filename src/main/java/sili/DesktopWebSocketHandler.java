package sili;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DesktopWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DesktopWebSocketHandler.class);

    // Expected shared secret from application.properties / Railway env var
    @Value("${server.desktop.sharedAuth:}")
    private String expectedToken;

    // active desktop sessions keyed by clientId
    //private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final WebSocketRegistry registry;

    public DesktopWebSocketHandler(WebSocketRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        Map<String, String> q = parseQuery(uri == null ? null : uri.getQuery());

        String clientId = trimToNull(q.get("clientId"));
        // Accept either ?authToken=... or ?auth=...
        String auth = trimToNull(firstNonBlank(q.get("authToken"), q.get("auth")));

        // Log lengths only (no secrets)
        log.info("WS handshake from {}  clientId='{}'  token.len={}  expected.len={}",
                safeRemote(session),
                clientId,
                auth == null ? 0 : auth.length(),
                expectedToken == null ? 0 : expectedToken.trim().length());

        if (clientId == null) {
            close(session, CloseStatus.POLICY_VIOLATION, "missing clientId");
            return;
        }
        if (isBlank(expectedToken)) {
            close(session, CloseStatus.POLICY_VIOLATION, "server token not configured");
            return;
        }
        if (auth == null || !expectedToken.trim().equals(auth)) {
            close(session, CloseStatus.POLICY_VIOLATION, "missing or invalid clientId/auth");
            return;
        }

        registry.registerDesktop(clientId, session);
        session.getAttributes().put("clientId", clientId);
        log.info("WS accepted for clientId='{}'", clientId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String clientId = (String) session.getAttributes().get("clientId");
        log.info("WS recv from clientId='{}': {}", clientId, message.getPayload());
        // TODO: handle ACKs or messages from desktop if needed
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String clientId = (String) session.getAttributes().get("clientId");
        if (clientId != null) {
        	registry.unregisterDesktop(session);
           // sessions.remove(clientId);
            log.info("WS closed for clientId='{}': {}", clientId, status);
        } else {
            log.info("WS closed (no clientId): {}", status);
        }
    }

    // -------- helpers --------

    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static void close(WebSocketSession s, CloseStatus st, String reason) {
        try { s.close(new CloseStatus(st.getCode(), reason)); } catch (Exception ignored) {}
    }

    private static String safeRemote(WebSocketSession s) {
        try { return String.valueOf(s.getRemoteAddress()); } catch (Exception e) { return "unknown"; }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> out = new HashMap<>();
        if (query == null || query.isEmpty()) return out;
        for (String pair : query.split("&")) {
            int i = pair.indexOf('=');
            String k = i >= 0 ? pair.substring(0, i) : pair;
            String v = i >= 0 ? pair.substring(i + 1) : "";
            // decode safely
            k = URLDecoder.decode(k, StandardCharsets.UTF_8);
            v = URLDecoder.decode(v, StandardCharsets.UTF_8);
            out.put(k, v);
        }
        return out;
    }

    // Expose a way to push to a connected desktop (by clientId)
    public boolean sendToDesktop(String clientId, String json) {
        WebSocketSession s = registry.getDesktop(clientId);
        if (s == null || !s.isOpen()) return false;
        try {
            s.sendMessage(new TextMessage(json));
            return true;
        } catch (Exception e) {
            log.warn("sendToDesktop failed for clientId='{}': {}", clientId, e.getMessage());
            return false;
        }
    }
}
