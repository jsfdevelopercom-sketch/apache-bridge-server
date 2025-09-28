package sili;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * Trivial redirect to a Google Sheets CSV export URL template.
 * You can enhance this to pick different gid by month, etc.
 */
@Controller
public class DownloadController {

    private final String csvTemplate;

    public DownloadController(@Value("${sheets.public.csvUrlTemplate:}") String csvTemplate) {
        this.csvTemplate = csvTemplate == null ? "" : csvTemplate.trim();
    }

    @GetMapping("/api/download/month")
    public void downloadMonth(HttpServletResponse resp) throws Exception {
        if (csvTemplate.isBlank()) {
            resp.sendError(HttpStatus.NOT_FOUND.value(), "No public CSV export URL configured");
            return;
        }
        // For now, just redirect. You can add ym=YYYY-MM and switch gid if needed.
        resp.setStatus(HttpStatus.FOUND.value()); // 302
        resp.setHeader("Location", csvTemplate);
    }
}
