package net.linktofur.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 直接拆成微服务了 比较方便
    public static void send(String title, String text, String email, boolean isHtml) throws Exception {
        String payload = MAPPER.writeValueAsString(Map.of(
                "token", Main.configs.get("notifyToken"),
                "to", email,
                "subject", title,
                "body", text,
                "is_html", isHtml // 标头
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Main.configs.get("notifyURL")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("邮件发送失败，状态码: " + response.statusCode() + "，响应: " + response.body());
        }
    }

    public static void send(String title, String text, User user) throws Exception {
        send(title, text, user.email, true);
    }

    public static void send(String title, String text, String email) throws Exception {
        send(title, text, email, false);
    }
}