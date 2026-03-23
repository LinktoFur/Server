package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;

import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.User;
import net.linktofur.user.UserType;
import net.linktofur.user.UserManager;
import net.linktofur.util.NotifyUtil;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@SuppressWarnings({ "DataFlowIssue", "unused" })
@Slf4j
public class RegisterAPI extends API {
    public RegisterAPI() {
        super("/user/register");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");

        if (isNull(email, name)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }
        var userByEmail = UserManager.INSTANCE.getUserByEmail(email);

        if (userByEmail != null && userByEmail.verified) {
            return Response.error(400, Map.of("message", "邮箱已被注册"));
        }

        if (name.length() < 3) {
            return Response.error(400, Map.of("message", "用户名长度必须至少为3"));
        }

        if (!email.matches("^[0-9]+@qq\\.com$")) {
            return Response.error(400, Map.of("message", "仅支持QQ邮箱"));
        }

        if (!userByEmail.verified) {
            NotifyUtil.INSTANCE.send("Linktofur.net - 注册通知",
                    "您尝试重新注册一个已存在但未验证的账户 以下是系统记录的旧信息:\n" + userByEmail, userByEmail);
            UserManager.INSTANCE.removeUser(userByEmail);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .level(UserType.NORMAL)
                .build();

        UserManager.INSTANCE.addUser(user);

        log.info("User registered: {}", user);

        return Response.success(Map.of("message", "注册成功"));
    }
}