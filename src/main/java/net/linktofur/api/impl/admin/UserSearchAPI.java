package net.linktofur.api.impl.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.UserManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/28
 */
@SuppressWarnings({"unused"})
@Slf4j
public class UserSearchAPI extends API {
    private final ObjectMapper MAPPER = new ObjectMapper();

    public UserSearchAPI() {
        super("admin/usersearch");
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

        var query = ctx.formParam("query");
        if (query == null) query = ctx.queryParam("query");

        if (isNull(query)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var keyword = query.toLowerCase();
        var results = UserManager.INSTANCE.users.values().stream()
                .filter(u -> u.verified && !u.banned)
                .filter(u -> u.name.toLowerCase().contains(keyword)
                        || u.email.toLowerCase().contains(keyword))
                .limit(20)
                .map(u -> Map.of(
                        "id", u.id.toString(),
                        "name", u.name,
                        "email", u.email,
                        "level", u.level.name()
                ))
                .toList();

        return Response.success(Map.of(
                "message", "查询成功",
                "users", MAPPER.writeValueAsString(results)
        ));
    }
}
