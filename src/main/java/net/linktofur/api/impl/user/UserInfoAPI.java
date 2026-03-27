package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings({"unused"})
public class UserInfoAPI extends API {
    public UserInfoAPI() {
        super("user/me");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        return Response.success(Map.of(
                "id", user.id.toString(),
                "name", user.name,
                "email", user.email,
                "level", user.level.name(),
                "banned", String.valueOf(user.banned),
                "verified", String.valueOf(user.verified),
                "createdAt", String.valueOf(user.createdAt)
        ));
    }
}