package cn.langya.api.impl.user;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import cn.langya.api.API;
import cn.langya.api.Response;
import cn.langya.user.User;
import cn.langya.user.UserLevel;
import cn.langya.user.UserManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
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