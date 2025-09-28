package sili;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DesktopWebSocketHandler desktopWebSocketHandler;

    public WebSocketConfig(DesktopWebSocketHandler desktopWebSocketHandler) {
        this.desktopWebSocketHandler = desktopWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Desktop client connects here:
        registry.addHandler(desktopWebSocketHandler, "/ws/desktop")
                .setAllowedOrigins("*"); // or restrict if you prefer
    }
}
