package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.util.NotifyUtil;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
public class VerifyAPI extends API {
    public VerifyAPI() {
        super("user/verify");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);
        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }
        if (user.verified) {
            return Response.error(400, Map.of("message", "已验证"));
        }

        String verifyCode = ctx.formParam("verifyCode");
        if (verifyCode == null) {
            NotifyUtil.send("Linktofur.net - 验证通知", "您的验证码为: " + user.getVerifyCode(), user.email);
            return Response.success(Map.of("message", "验证码已发送到邮箱"));
        }

        if (verifyCode.equals(user.getVerifyCode())) {
            user.verified = true;
            return Response.success(Map.of("message", "验证成功"));
        } else {
            return Response.error(400, Map.of("message", "验证码错误"));
        }
    }
}