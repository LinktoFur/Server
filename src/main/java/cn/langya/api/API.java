package cn.langya.api;

import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import cn.langya.user.User;
import cn.langya.user.UserManager;
import cn.langya.user.session.SessionManager;

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

        return UserManager.INSTANCE.getUserById(userId);
    }
}
