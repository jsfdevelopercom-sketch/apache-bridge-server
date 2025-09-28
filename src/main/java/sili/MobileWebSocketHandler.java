package sili;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * Handles WebSocket connections from bedside/mobile clients.
 */
@Component
public class MobileWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRegistry registry;

    public MobileWebSocketHandler(WebSocketRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String,String> q = QueryUtil.parse(session.getUri().toString());
        String clientId = q.getOrDefault("clientId","UNKNOWN");
        registry.registerMobile(clientId, session);
        session.sendMessage(new TextMessage("{\"connected\":\"mobile\",\"clientId\":\""+clientId+"\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.unregisterMobile(session);
    }
}
