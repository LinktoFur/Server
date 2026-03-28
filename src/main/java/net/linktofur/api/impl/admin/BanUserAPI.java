package net.linktofur.api.impl.admin;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.user.UserManager;
import net.linktofur.util.NotifyUtil;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class BanUserAPI extends API {
    public BanUserAPI() {
        super("admin/ban");
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
            return Response.error(400, Map.of("message", "无法封禁自己"));
        }

        var targetUser = UserManager.INSTANCE.getUserById(userId);

        if (isNull(targetUser)) {
            return Response.error(404, Map.of("message", "用户不存在"));
        }

        targetUser.banned = true;
        PersistenceManager.INSTANCE.save();

        log.info("User {} banned by {}", targetUser.name, user.name);

        NotifyUtil.MAIL.send("Linktofur.net - 账号封禁通知",
                "您的账号已被管理员封禁，如有疑问请联系管理员。", targetUser);

        NotifyUtil.BOT.send(String.format(
                "用户封禁\n操作者: %s（管理员）\n邮箱: %s\n\n被封禁用户: %s\n邮箱: %s",
                user.name, user.email, targetUser.name, targetUser.email));

        return Response.success(Map.of("message", "封禁成功"));
    }
}
