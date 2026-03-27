package net.linktofur.api;

import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import net.linktofur.user.User;
import net.linktofur.user.UserManager;
import net.linktofur.user.session.SessionManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@AllArgsConstructor
public abstract class API {
    public String name;

    public Response run(Context ctx) throws Exception {
        return null;
    }

    public boolean isNull(Object... objects) {
        for (Object object : objects) {
            switch (object) {
                case null -> {
                    return true;
                }
                case String string when string.isEmpty() -> {
                    return true;
                }
                case List<?> list when list.isEmpty() -> {
                    return true;
                }
                default -> {
                }
            }
        }
        return false;
    }

    public User getUser(Context ctx) {
        // 优先 cookie 其次 Authorization header
        String sessionIdStr = ctx.cookie("sessionId");
        if (sessionIdStr == null) {
            String authHeader = ctx.header("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                sessionIdStr = authHeader.substring(7);
            }
        }

        if (sessionIdStr == null) {
            ctx.attribute("_authError", Response.error(401, Map.of("message", "未登入")));
            return null;
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            ctx.attribute("_authError", Response.error(401, Map.of("message", "无效 Session")));
            return null;
        }

        var userId = SessionManager.INSTANCE.getUserId(sessionId);

        if (userId == null) {
            ctx.attribute("_authError", Response.error(401, Map.of("message", "无效 Session")));
            return null;
        }

        var user = UserManager.INSTANCE.getUserById(userId);
        if (user == null) {
            ctx.attribute("_authError", Response.error(401, Map.of("message", "用户不存在")));
            return null;
        }

        if (!user.verified) {
            ctx.attribute("_authError", Response.error(403, Map.of("message", "未验证")));
            return null;
        }

        if (user.banned) {
            ctx.attribute("_authError", Response.error(403, Map.of("message", "你被封了")));
            return null;
        }

        ctx.attribute("_sessionId", sessionId);

        return user;
    }

    public Response authError(Context ctx) {
        Response err = ctx.attribute("_authError");
        return err != null ? err : Response.error(401, Map.of("message", "未登入"));
    }
}