package net.linktofur.api.impl.user;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.user.UserManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@SuppressWarnings("DataFlowIssue")
@Slf4j
public class BanUserAPI extends API {
    public BanUserAPI() {
        super("user/ban");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }

        if (!user.isAdmin()) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        String rawUserId = ctx.formParam("userId");

        if (isNull(rawUserId)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        UUID userId;
        try {
            userId = UUID.fromString(rawUserId);
        } catch (IllegalArgumentException e) {

            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var targetUser = UserManager.INSTANCE.getUserById(userId);

        if (isNull(targetUser)) {
            return Response.error(404, Map.of("message", "用户不存在"));
        }

        return Response.success(Map.of("message", "封禁成功"));
    }
}