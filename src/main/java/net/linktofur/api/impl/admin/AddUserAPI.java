package net.linktofur.api.impl.admin;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.User;
import net.linktofur.user.UserType;
import net.linktofur.user.UserManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/28
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class AddUserAPI extends API {
    public AddUserAPI() {
        super("admin/adduser");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        if (!user.isAdmin()) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        var email = ctx.formParam("email");
        var name = ctx.formParam("name");

        if (isNull(email, name)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        if (!email.matches("^[0-9]+@qq\\.com$")) {
            return Response.error(400, Map.of("message", "仅支持QQ邮箱"));
        }

        var existing = UserManager.INSTANCE.getUserByEmail(email);
        if (existing != null && existing.verified) {
            return Response.error(400, Map.of("message", "该邮箱已被注册"));
        }

        // 如果有未验证的同邮箱用户 先移除
        if (existing != null) {
            UserManager.INSTANCE.removeUser(existing);
        }

        User newUser = User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .level(UserType.NORMAL)
                .verified(true)
                .build();

        UserManager.INSTANCE.addUser(newUser);

        log.info("Admin {} added user: {} ({})", user.name, name, email);

        return Response.success(Map.of(
                "message", "用户添加成功",
                "userId", newUser.id.toString(),
                "name", newUser.name,
                "email", newUser.email
        ));
    }
}
