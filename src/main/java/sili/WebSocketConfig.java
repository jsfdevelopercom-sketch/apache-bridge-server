package sili;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DesktopWebSocketHandler desktopHandler;
    private final MobileWebSocketHandler mobileHandler;

    public WebSocketConfig(DesktopWebSocketHandler desktopHandler,
                           MobileWebSocketHandler mobileHandler) {
        this.desktopHandler = desktopHandler;
        this.mobileHandler = mobileHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(desktopHandler, "/ws/desktop").setAllowedOrigins("*");
        registry.addHandler(mobileHandler, "/ws/mobile").setAllowedOrigins("*");
    }
}
