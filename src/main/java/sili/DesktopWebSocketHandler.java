package sili;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;

/**
 * Handles WebSocket connections from nursing station desktop clients.
 */
@Component
public class DesktopWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRegistry registry;
    private final String sharedAuth;

    public DesktopWebSocketHandler(WebSocketRegistry registry,
                                   @Value("${server.desktop.sharedAuth:}") String sharedAuth) {
        this.registry = registry;
        this.sharedAuth = sharedAuth;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String,String> q = QueryUtil.parse(session.getUri().toString());
        String clientId = q.get("clientId");
        String auth = q.get("auth");
        if (clientId == null || auth == null || !auth.equals(sharedAuth)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing or invalid clientId/auth"));
            return;
        }
        registry.registerDesktop(clientId, session);
        session.sendMessage(new TextMessage("{\"connected\":\"desktop\",\"clientId\":\""+clientId+"\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String,String> q = QueryUtil.parse(session.getUri().toString());
        String clientId = q.get("clientId");
        if (clientId == null) return;
        Set<WebSocketSession> mobileSessions = registry.getMobiles(clientId);
        for (WebSocketSession m : mobileSessions) {
            if (m.isOpen()) m.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.unregisterDesktop(session);
    }
}
