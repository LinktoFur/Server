package cn.langya.api;

import lombok.Builder;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@Builder
public class Response {
    public int code;
    public Map<String, String> message;

    public static Response success(Map<String, String> message) {
        return new Response(200, message);
    }

    public static Response error(int code, Map<String, String> message) {
        return new Response(code, message);
    }
}
