package net.linktofur.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.Main;
import net.linktofur.user.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
public class NotifyUtil {
    public static final Mail MAIL = new Mail();
    public static final Bot BOT = new Bot();

    @Slf4j
    public static class Mail {
        private final HttpClient client = HttpClient.newHttpClient();
        private final ObjectMapper mapper = new ObjectMapper();

        public void send(String title, String text, String email, boolean isHtml) {
            try {
                String payload = mapper.writeValueAsString(Map.of(
                        "token", Main.configs.get("notifyToken"),
                        "to", email,
                        "subject", title,
                        "body", text,
                        "is_html", isHtml
                ));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(Main.configs.get("notifyURL")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() != 200) {
                                log.warn("邮件发送失败 to={} status={} body={}", email, response.statusCode(), response.body());
                            }
                        })
                        .exceptionally(e -> {
                            log.error("邮件发送异常 to={}", email, e);
                            return null;
                        });
            } catch (Exception e) {
                log.error("邮件构建失败 to={}", email, e);
            }
        }

        public void send(String title, String text, User user) {
            send(title, text, user.email, true);
        }

        public void send(String title, String text, String email) {
            send(title, text, email, false);
        }
    }

    @Slf4j
    public static class Bot {
        private final HttpClient client = HttpClient.newHttpClient();
        private final ObjectMapper mapper = new ObjectMapper();

        public void send(String message) {
            try {
                var baseUrl = Main.configs.get("onebotBaseUrl");
                var token = Main.configs.get("onebotToken");
                var groupId = Main.configs.get("onebotGroupId");

                if (baseUrl == null || baseUrl.isEmpty() || groupId == null || groupId.isEmpty()) {
                    log.warn("OneBot not configured, skipping bot notification");
                    return;
                }

                var url = baseUrl.endsWith("/") ? baseUrl + "send_group_msg" : baseUrl + "/send_group_msg";
                var jsonBody = mapper.writeValueAsString(Map.of(
                        "group_id", Long.parseLong(groupId),
                        "message", message
                ));

                var requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json");

                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                var request = requestBuilder
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                log.info("OneBot notification sent successfully");
                            } else {
                                log.warn("OneBot notification failed: {} {}", response.statusCode(), response.body());
                            }
                        })
                        .exceptionally(e -> {
                            log.error("Failed to send OneBot notification", e);
                            return null;
                        });
            } catch (Exception e) {
                log.error("Failed to send OneBot notification", e);
            }
        }
    }
}
