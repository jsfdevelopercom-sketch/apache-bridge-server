package sili;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Minimal "Online" appender that POSTs the raw JSON (plus a shared secret)
 * to a Google Apps Script webhook which writes to Google Sheets.
 */
@Service
public class OnlineAppendService {

    private final boolean enabled;
    private final String webhookUrl;
    private final String shared;

    private final HttpClient http = HttpClient.newBuilder().build();

    public OnlineAppendService(
            @Value("${online.enabled:false}") boolean enabled,
            @Value("${sheets.webhook.url:}") String webhookUrl,
            @Value("${sheets.webhook.secret:}") String shared) {
        this.enabled = enabled;
        this.webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();
        this.shared = shared == null ? "" : shared.trim();
    }

    /** Returns null on success, or an error message on failure. */
    public String tryAppend(String rawJsonBody) {
        if (!enabled || webhookUrl.isBlank() || shared.isBlank()) {
            return "online append not configured";
        }
        try {
            // Build a tiny envelope: { "secret":"...", "payload": <raw JSON> }
            String body = "{\"secret\":\"" + esc(shared) + "\",\"payload\":" + rawJsonBody + "}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return null; // success
            }
            return "webhook returned " + resp.statusCode() + " -> " + resp.body();
        } catch (Exception e) {
            return "webhook error: " + e.getClass().getSimpleName() + ": " +
                    (e.getMessage() == null ? "(no message)" : e.getMessage());
        }
    }

    public boolean isConfigured() {
        return enabled && !webhookUrl.isBlank() && !shared.isBlank();
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

