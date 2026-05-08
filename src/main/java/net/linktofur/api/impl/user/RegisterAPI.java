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

    private static final java.util.regex.Pattern NAME_PATTERN =
            java.util.regex.Pattern.compile("^[\\p{IsHan}A-Za-z0-9_\\-]{2,32}$");

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

            var result = user.consumeVerifyCode(verifyCode);
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

            user.verified = true;
            user.loginAt = System.currentTimeMillis();
            PersistenceManager.INSTANCE.markDirty();

            UUID sessionId = SessionManager.INSTANCE.createSession(user.id);
            Cookie cookie = new Cookie("sessionId", sessionId.toString());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setSameSite(SameSite.LAX);
            cookie.setMaxAge(30 * 24 * 3600);
            ctx.cookie(cookie);

            log.info("User registered and verified: id={} name={}", user.id, user.name);
            return Response.success(Map.of(
                    "message", "注册成功",
                    "sessionId", sessionId.toString(),
                    "name", user.name,
                    "email", user.email,
                    "level", user.level.name()
            ));
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
        if (!NAME_PATTERN.matcher(name).matches()) {
            return Response.error(400, Map.of("message", "用户名仅支持中文/字母/数字/下划线/横线 长度 2-32"));
        }

        var existing = UserManager.INSTANCE.getUserByEmail(email);
        if (existing != null && existing.verified) {
            return Response.error(400, Map.of("message", "邮箱已被注册"));
        }

        // unverified 邮箱:不删除原账号 仅重发验证码 同时受 60 秒冷却限制
        // 防止"删除 + 第一人提权"链路 也防止他人覆盖你的待验证账号
        User user;
        if (existing != null) {
            if (!existing.canIssueCode()) {
                return Response.error(429, Map.of("message", "请稍后再试"));
            }
            user = existing;
            user.name = name;
        } else {
            user = User.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .email(email)
                    .level(UserType.NORMAL)
                    .build();
            UserManager.INSTANCE.addUser(user);
        }

        user.issueVerifyCode();
        PersistenceManager.INSTANCE.markDirty();
        NotifyUtil.MAIL.send("Linktofur.net - 注册验证", "您的验证码为: " + user.verifyCode, user);

        return Response.success(Map.of("message", "验证码已发送到邮箱"));
    }
}