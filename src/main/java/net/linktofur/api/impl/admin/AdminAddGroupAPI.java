package net.linktofur.api.impl.admin;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.Main;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.Group;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;
import net.linktofur.user.UserManager;
import net.linktofur.util.NotifyUtil;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/28
 */
@Slf4j
@SuppressWarnings({"DataFlowIssue", "unused"})
public class AdminAddGroupAPI extends API {
    public AdminAddGroupAPI() {
        super("admin/addgroup");
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

        var userId = ctx.formParam("userId");
        var groupId = ctx.formParam("groupId");
        var groupName = ctx.formParam("groupName");
        var orgName = ctx.formParam("orgName");
        var region = ctx.formParam("region");
        var type = ctx.formParam("type");
        var joinEntry = ctx.formParam("joinEntry");

        if (isNull(userId, groupId, groupName, orgName, region, type, joinEntry)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var groupType = GroupType.parse(type);
        if (groupType == null) {
            return Response.error(400, Map.of("message", "无效的群类型"));
        }

        UUID targetUserId;
        try {
            targetUserId = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return Response.error(400, Map.of("message", "无效的用户ID"));
        }

        var targetUser = UserManager.INSTANCE.getUserById(targetUserId);
        if (targetUser == null) {
            return Response.error(404, Map.of("message", "指定的用户不存在"));
        }

        var group = Group.builder()
                .groupId(groupId).groupName(groupName).orgName(orgName)
                .userId(targetUserId).region(region).type(groupType).joinEntry(joinEntry)
                .pending(false)
                .build();
        GroupManager.INSTANCE.addGroup(group);

        log.info("Admin {} added group {} for user {}", user.name, groupName, targetUser.name);

        // 给指定用户发送审核成功邮件
        var content = String.format(
                "管理员已为您添加群组「%s」，该群组已通过审核，现已公开显示在 %s",
                groupName, Main.url
        );
        NotifyUtil.MAIL.send("Linktofur.net - 审核通过通知", content, targetUser);

        return Response.success(Map.of("message", "群组添加成功，已通知用户"));
    }
}
