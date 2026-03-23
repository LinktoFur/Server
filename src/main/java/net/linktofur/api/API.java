package net.linktofur.api;

import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import net.linktofur.user.User;
import net.linktofur.user.UserManager;
import net.linktofur.user.session.SessionManager;

import java.util.List;
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
            if (object instanceof String string) {
                return string.isEmpty();
            }
            if (object == null) {
                return true;
            }
            if (object instanceof List<?> list) {
                return list.isEmpty();
            }
        }
        return false;
    }

    public User getUser(Context ctx) {
        String sessionIdStr = ctx.cookie("sessionId");
        if (sessionIdStr == null) {
            ctx.status(401).result("未登入");
            return null;
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            ctx.status(401).result("无效 Session");
            return null;
        }

        var userId = SessionManager.INSTANCE.getUserId(sessionId);

        if (userId == null) {
            ctx.status(401).result("无效 Session");
            return null;
        }

        var user = UserManager.INSTANCE.getUserById(userId);
        if (!user.verified) {
            ctx.status(403).result("未验证");
            return null;
        }

        if (user.banned) {
            ctx.status(403).result("你被封了");
            return null;
        }

        return user;
    }
}
