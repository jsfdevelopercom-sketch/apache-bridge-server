package sili;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of connected desktop and mobile sessions.
 */
@Component
public class WebSocketRegistry {
    private final Map<String,WebSocketSession> desktopSessions = new ConcurrentHashMap<>();
    private final Map<String,Set<WebSocketSession>> mobileSessionsByClient = new ConcurrentHashMap<>();

    public void registerDesktop(String clientId, WebSocketSession session){
        desktopSessions.put(clientId, session);
    }
    public void unregisterDesktop(WebSocketSession session){
        desktopSessions.entrySet().removeIf(e -> e.getValue().getId().equals(session.getId()));
    }
    public WebSocketSession getDesktop(String clientId){
        return desktopSessions.get(clientId);
    }

    public void registerMobile(String clientId, WebSocketSession session){
        mobileSessionsByClient.computeIfAbsent(clientId,
            k -> ConcurrentHashMap.newKeySet()).add(session);
    }
    public void unregisterMobile(WebSocketSession session){
        for (Set<WebSocketSession> set : mobileSessionsByClient.values()) {
            set.removeIf(s -> s.getId().equals(session.getId()));
        }
        mobileSessionsByClient.entrySet().removeIf(e -> e.getValue().isEmpty());
    }
    public Set<WebSocketSession> getMobiles(String clientId) {
        return mobileSessionsByClient.getOrDefault(clientId, Collections.emptySet());
    }
}

