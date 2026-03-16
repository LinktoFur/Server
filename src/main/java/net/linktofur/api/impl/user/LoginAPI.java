package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.User;
import org.mindrot.jbcrypt.BCrypt;
import net.linktofur.user.UserManager;
import net.linktofur.user.session.SessionManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
@Slf4j
public class LoginAPI extends API {
    public LoginAPI() {
        super("/user/login");
    }

    @Override
    public Response run(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        if (isNull(email, password)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        User user = UserManager.INSTANCE.getUserByEmail(email);
        if (user == null) {
            return Response.error(401, Map.of("message", "账户不存在或密码错误"));
        }

        if (!BCrypt.checkpw(password, user.password)) {
            return Response.error(401, Map.of("message", "账户不存在或密码错误"));
        }

        UUID sessionId = SessionManager.INSTANCE.createSession(user.id);

        Cookie cookie = new Cookie("sessionId", sessionId.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(7 * 24 * 3600); // 7 days

        ctx.cookie(cookie);

        user.loginAt = System.currentTimeMillis();

        return Response.success(Map.of(
                "message", "登录成功",
                "name", user.name,
                "email", user.email
        ));
    }
}