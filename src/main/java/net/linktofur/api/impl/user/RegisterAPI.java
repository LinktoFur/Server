package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.mindrot.jbcrypt.BCrypt;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.User;
import net.linktofur.user.UserLevel;
import net.linktofur.user.UserManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@SuppressWarnings("DataFlowIssue")
@Slf4j
public class RegisterAPI extends API {
    public RegisterAPI() {
        super("/user/register");
    }

    @Override
    public Response run(Context ctx) {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        if (isNull(email, name, password)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }
        var userByEmail = UserManager.INSTANCE.getUserByEmail(email);

        if (userByEmail != null && userByEmail.verified) {
            return Response.error(400, Map.of("message", "邮箱已被注册"));
        }

        if (name.length() < 3) {
            return Response.error(400, Map.of("message", "用户名长度必须至少为3"));
        }

        if (password.length() < 6) {
            return Response.error(400, Map.of("message", "密码长度必须至少为6"));
        }

        if (!email.matches("^[0-9]+@qq\\.com$")) {
            return Response.error(400, Map.of("message", "仅支持QQ邮箱"));
        }

        if (!userByEmail.verified) {
            // TODO 发旧账户全部信息邮件给他
            var message = userByEmail.toString();
            UserManager.INSTANCE.removeUser(userByEmail);
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .password(hashedPassword)
                .level(UserLevel.NORMAL)
                .build();

        UserManager.INSTANCE.addUser(user);

        log.info("User registered: {}", user);

        return Response.success(Map.of("message", "注册成功"));
    }
}