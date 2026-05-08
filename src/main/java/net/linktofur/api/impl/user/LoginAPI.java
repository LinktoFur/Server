package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.captcha.CaptchaManager;
import net.linktofur.user.User;
import net.linktofur.util.NotifyUtil;
import net.linktofur.database.PersistenceManager;
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
        super("user/login");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        String email = ctx.formParam("email");
        String code = ctx.formParam("token");

        if (isNull(email)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        if (code == null) {
            String captchaId = ctx.formParam("captchaId");
            String captchaAnswer = ctx.formParam("captchaAnswer");
            if (!CaptchaManager.INSTANCE.validate(captchaId, captchaAnswer)) {
                return Response.error(400, Map.of("message", "人机验证失败"));
            }

            User user = UserManager.INSTANCE.getUserByEmail(email);
            if (user != null && user.verified && !user.banned && user.canIssueCode()) {
                user.issueVerifyCode();
                NotifyUtil.MAIL.send("Linktofur.net - 登录验证", "您的登录验证码为: " + user.verifyCode, user);
                PersistenceManager.INSTANCE.markDirty();
            }
            // 不区分账户是否存在 防止枚举
            return Response.success(Map.of("message", "如果该邮箱已注册 验证码已发送"));
        }

        User user = UserManager.INSTANCE.getUserByEmail(email);
        if (user == null) {
            return Response.error(400, Map.of("message", "验证码错误或已失效"));
        }

        var result = user.consumeVerifyCode(code);
        PersistenceManager.INSTANCE.markDirty();
        switch (result) {
            case OK -> {}
            case EXPIRED, LOCKED, MISSING -> {
                return Response.error(400, Map.of("message", "验证码已失效 请重新获取"));
            }
            case MISMATCH -> {
                return Response.error(400, Map.of("message", "验证码错误"));
            }
        }

        UUID sessionId = SessionManager.INSTANCE.createSession(user.id);

        Cookie cookie = new Cookie("sessionId", sessionId.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(30 * 24 * 3600);
        ctx.cookie(cookie);

        user.loginAt = System.currentTimeMillis();
        PersistenceManager.INSTANCE.markDirty();

        return Response.success(Map.of(
                "message", "登录成功",
                "name", user.name,
                "email", user.email,
                "sessionId", sessionId.toString(),
                "level", user.level.name()
        ));
    }
}