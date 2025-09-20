package sili;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowedOrigins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Always pass a String[] to allowedOrigins(...)
        String[] origins = "*".equals(allowedOrigins)
                ? new String[] {"*"}
                : Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(origins)      // OK: always String[]
                .allowedMethods("GET","POST","OPTIONS")
                .allowCredentials(false);     // must be false if using "*"
    }
}
