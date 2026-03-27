package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.extern.slf4j.Slf4j;

import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.captcha.CaptchaManager;
import net.linktofur.database.PersistenceManager;
import net.linktofur.user.User;
import net.linktofur.user.UserType;
import net.linktofur.user.UserManager;
import net.linktofur.user.session.SessionManager;
import net.linktofur.util.NotifyUtil;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class RegisterAPI extends API {
    public RegisterAPI() {
        super("user/register");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        String email = ctx.formParam("email");

        if (isNull(email)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        String verifyCode = ctx.formParam("verifyCode");
        if (verifyCode != null) {
            var user = UserManager.INSTANCE.getUserByEmail(email);

            if (user == null) {
                return Response.error(400, Map.of("message", "请先提交注册"));
            }

            if (user.verified) {
                return Response.error(400, Map.of("message", "邮箱已被注册"));
            }

            if (verifyCode.equals(user.getVerifyCode())) {
                user.verified = true;
                user.verifyCode = null;
                user.loginAt = System.currentTimeMillis();
                PersistenceManager.INSTANCE.save();

                // 直接创建会话 不用再登录
                UUID sessionId = SessionManager.INSTANCE.createSession(user.id);

                Cookie cookie = new Cookie("sessionId", sessionId.toString());
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setSameSite(SameSite.LAX);
                cookie.setMaxAge(30 * 24 * 3600);
                ctx.cookie(cookie);

                log.info("User registered and verified: {}", user);
                return Response.success(Map.of(
                        "message", "注册成功",
                        "sessionId", sessionId.toString(),
                        "name", user.name,
                        "email", user.email,
                        "level", user.level.name()
                ));
            } else {
                return Response.error(400, Map.of("message", "验证码错误"));
            }
        }

        String name = ctx.formParam("name");

        if (isNull(name)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        String captchaId = ctx.formParam("captchaId");
        String captchaAnswer = ctx.formParam("captchaAnswer");

        if (!CaptchaManager.INSTANCE.validate(captchaId, captchaAnswer)) {
            return Response.error(400, Map.of("message", "人机验证失败"));
        }

        if (!email.matches("^[0-9]+@qq\\.com$")) {
            return Response.error(400, Map.of("message", "仅支持QQ邮箱"));
        }

        var userByEmail = UserManager.INSTANCE.getUserByEmail(email);

        if (userByEmail != null && userByEmail.verified) {
            return Response.error(400, Map.of("message", "邮箱已被注册"));
        }

        if (userByEmail != null && !userByEmail.verified) {
            UserManager.INSTANCE.removeUser(userByEmail);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .level(UserType.NORMAL)
                .build();

        UserManager.INSTANCE.addUser(user);

        NotifyUtil.MAIL.send("Linktofur.net - 注册验证", "您的验证码为: " + user.getVerifyCode(), user);

        return Response.success(Map.of("message", "验证码已发送到邮箱"));
    }
}