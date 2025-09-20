
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * HTTP endpoint for receiving submissions from the mobile client.
 */
@RestController
@RequestMapping("/api")
public class SubmitController {

    private final WebSocketRegistry registry;

    @Value("${server.desktop.sharedAuth:}")
    private String sharedAuth;

    public SubmitController(WebSocketRegistry registry) {
        this.registry = registry;
    }

    @PostMapping(value="/submit", consumes="application/json", produces="application/json")
    public ResponseEntity<?> submit(@RequestBody ApachePayload payload) {
        if (payload == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"empty payload\"}");
        }
        if (payload.clientId == null || payload.authToken == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"missing clientId or authToken\"}");
        }
        if (!payload.authToken.equals(sharedAuth)) {
            return ResponseEntity.status(403).body("{\"error\":\"invalid auth token\"}");
        }
        WebSocketSession desktop = registry.getDesktop(payload.clientId);
        if (desktop == null || !desktop.isOpen()) {
            return ResponseEntity.status(503).body("{\"error\":\"desktop not connected\"}");
        }

        try {
            String json = JsonUtil.toJson(payload);
            desktop.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"failed to forward to desktop\",\"detail\":\""+e.getMessage()+"\"}");
        }
        return ResponseEntity.accepted()
                .body("{\"status\":\"forwarded\",\"clientId\":\""+payload.clientId+"\"}");
    }
}

