package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.session.SessionManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
public class LogOutAPI extends API {
    public LogOutAPI() {
        super("user/logout");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }

        user.logoutAt = System.currentTimeMillis();

        SessionManager.INSTANCE.removeSession(user.id);
        ctx.removeCookie("sessionId", "/");

        return Response.success(Map.of("message", "登出成功"));
    }
}
