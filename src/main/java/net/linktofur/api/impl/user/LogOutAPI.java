package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.user.session.SessionManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
@SuppressWarnings("unused")
public class LogOutAPI extends API {
    public LogOutAPI() {
        super("user/logout");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        user.logoutAt = System.currentTimeMillis();
        PersistenceManager.INSTANCE.save();

        UUID sessionId = ctx.attribute("_sessionId");
        if (sessionId != null) {
            SessionManager.INSTANCE.removeSession(sessionId);
        }
        Cookie cookie = new Cookie("sessionId", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(0);
        ctx.cookie(cookie);

        return Response.success(Map.of("message", "登出成功"));
    }
}