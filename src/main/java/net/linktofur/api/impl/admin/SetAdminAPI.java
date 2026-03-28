package net.linktofur.api.impl.admin;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.user.UserManager;
import net.linktofur.user.UserType;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/27
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class SetAdminAPI extends API {
    public SetAdminAPI() {
        super("admin/setadmin");
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

        if (userId.equals(user.id)) {
            return Response.error(400, Map.of("message", "无法操作自己"));
        }

        var targetUser = UserManager.INSTANCE.getUserById(userId);

        if (isNull(targetUser)) {
            return Response.error(404, Map.of("message", "用户不存在"));
        }

        if (targetUser.isAdmin()) {
            return Response.error(400, Map.of("message", "该用户已是管理员"));
        }

        targetUser.level = UserType.ADMIN;
        PersistenceManager.INSTANCE.save();

        log.info("User {} promoted to admin by {}", targetUser.name, user.name);

        return Response.success(Map.of("message", "已设为管理员"));
    }
}
