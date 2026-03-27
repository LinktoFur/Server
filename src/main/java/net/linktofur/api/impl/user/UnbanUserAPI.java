package net.linktofur.api.impl.user;

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
 * @date 2026/3/27
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class UnbanUserAPI extends API {
    public UnbanUserAPI() {
        super("user/unban");
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

        var targetUser = UserManager.INSTANCE.getUserById(userId);

        if (isNull(targetUser)) {
            return Response.error(404, Map.of("message", "用户不存在"));
        }

        if (!targetUser.banned) {
            return Response.error(400, Map.of("message", "该用户未被封禁"));
        }

        targetUser.banned = false;
        PersistenceManager.INSTANCE.save();

        log.info("User {} unbanned by {}", targetUser.name, user.name);

        NotifyUtil.MAIL.send("Linktofur.net - 账号解封通知",
                "您的账号已被管理员解封，现在可以正常使用了。", targetUser);

        NotifyUtil.BOT.send(String.format(
                "用户解封\n操作者: %s（管理员）\n邮箱: %s\n\n被解封用户: %s\n邮箱: %s",
                user.name, user.email, targetUser.name, targetUser.email));

        return Response.success(Map.of("message", "解封成功"));
    }
}
