package net.linktofur.api.impl.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.UserManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings({"unused"})
@Slf4j
public class UserListAPI extends API {
    private final ObjectMapper MAPPER = new ObjectMapper();

    public UserListAPI() {
        super("user/list");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        if (!user.isAdmin()) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        var users = UserManager.INSTANCE.users.values().stream()
                .map(u -> Map.of(
                        "id", u.id.toString(),
                        "name", u.name,
                        "email", u.email,
                        "level", u.level.name(),
                        "banned", String.valueOf(u.banned),
                        "verified", String.valueOf(u.verified),
                        "createdAt", String.valueOf(u.createdAt)
                ))
                .toList();

        return Response.success(Map.of(
                "message", "查询成功",
                "total", String.valueOf(users.size()),
                "users", MAPPER.writeValueAsString(users)
        ));
    }
}