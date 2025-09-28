package sili;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * HTTP endpoint for receiving submissions from the mobile client.
 * Raw JSON passthrough; preserves your prior logic and adds Online fallback.
 */
@RestController
@RequestMapping("/api")
public class SubmitController {

    private final WebSocketRegistry registry;
    private final ObjectMapper om = new ObjectMapper();
    private final OnlineAppendService online;

    @Value("${server.desktop.sharedAuth:}")
    private String sharedAuth;

    public SubmitController(WebSocketRegistry registry, OnlineAppendService online) {
        this.registry = registry;
        this.online = online;
    }

    @PostMapping(
            value = "/submit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> submit(@RequestBody String body) {
        if (body == null || body.trim().isEmpty()) {
            return json(HttpStatus.BAD_REQUEST, "{\"error\":\"empty payload\"}");
        }

        final String clientId;
        final String authToken;
        try {
            JsonNode root = om.readTree(body);
            clientId = textOrNull(root.get("clientId"));
            authToken = textOrNull(root.get("authToken"));
        } catch (Exception parseEx) {
            return json(HttpStatus.BAD_REQUEST, "{\"error\":\"invalid JSON\"}");
        }

        if (clientId == null || clientId.isBlank() || authToken == null || authToken.isBlank()) {
            return json(HttpStatus.BAD_REQUEST, "{\"error\":\"missing clientId or authToken\"}");
        }

        if (sharedAuth == null || sharedAuth.isBlank()) {
            return json(HttpStatus.SERVICE_UNAVAILABLE, "{\"error\":\"server auth not configured\"}");
        }

        if (!sharedAuth.equals(authToken)) {
            return json(HttpStatus.FORBIDDEN, "{\"error\":\"invalid auth token\"}");
        }

        WebSocketSession desktop = registry.getDesktop(clientId);
        if (desktop != null && desktop.isOpen()) {
            try {
                desktop.sendMessage(new TextMessage(body)); // forward as-is to desktop
                return json(HttpStatus.OK, "{\"status\":\"DELIVERED\",\"clientId\":\"" + esc(clientId) + "\"}");
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null) msg = "forward failed";
                return json(HttpStatus.BAD_GATEWAY,
                        "{\"error\":\"failed to forward to desktop\",\"detail\":\"" + esc(msg) + "\"}");
            }
        }

        // -------- NEW: Online fallback (Google Sheets via Apps Script) ----------
        if (online != null && online.isConfigured()) {
            String err = online.tryAppend(body);
            if (err == null) {
                return json(HttpStatus.OK, "{\"status\":\"ONLINE_APPENDED\",\"clientId\":\"" + esc(clientId) + "\"}");
            } else {
                return json(HttpStatus.SERVICE_UNAVAILABLE,
                        "{\"error\":\"desktop not connected; online append failed\",\"detail\":\"" + esc(err) + "\"}");
            }
        }

        // If online not configured, behave like before:
        return json(HttpStatus.SERVICE_UNAVAILABLE, "{\"error\":\"desktop not connected\"}");
    }

    // --- helpers (unchanged from your style) ---
    private static String textOrNull(JsonNode n) {
        if (n == null || n.isNull()) return null;
        String s = n.asText(null);
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static ResponseEntity<String> json(HttpStatus status, String body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
