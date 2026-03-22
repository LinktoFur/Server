package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.User;
import net.linktofur.util.NotifyUtil;
import org.mindrot.jbcrypt.BCrypt;
import net.linktofur.user.UserManager;
import net.linktofur.user.session.SessionManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
@SuppressWarnings("unused")
@Slf4j
public class LoginAPI extends API {
    public LoginAPI() {
        super("/user/login");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        String email = ctx.formParam("email");
        String code = ctx.formParam("token");

        if (isNull(email)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        User user = UserManager.INSTANCE.getUserByEmail(email);
        if (user == null) {
            return Response.error(401, Map.of("message", "账户不存在"));
        }

        if (code == null) {
            NotifyUtil.send("Linktofur.net - 登录验证", "您的登录验证码为: " + user.getVerifyCode(), user);
            return Response.success(Map.of("message", "验证码已发送到邮箱"));
        }

        if (code.equals(user.getVerifyCode())) {
            UUID sessionId = SessionManager.INSTANCE.createSession(user.id);

            Cookie cookie = new Cookie("sessionId", sessionId.toString());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setSameSite(SameSite.LAX);
            cookie.setMaxAge(30 * 24 * 3600); // 30 days

            ctx.cookie(cookie);

            user.loginAt = System.currentTimeMillis();
            user.verifyCode = null;

            return Response.success(Map.of(
                    "message", "登录成功",
                    "name", user.name,
                    "email", user.email
            ));
        } else {
            return Response.error(400, Map.of("message", "验证码错误"));
        }
    }
}