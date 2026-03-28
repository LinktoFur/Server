package net.linktofur.api.impl.admin;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.group.GroupManager;
import net.linktofur.util.NotifyUtil;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class RejectGroupAPI extends API {
    public RejectGroupAPI() {
        super("admin/reject");
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

        var id = ctx.formParam("id");

        if (isNull(id)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        int groupId;
        try {
            groupId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var group = GroupManager.INSTANCE.getGroupById(groupId);

        if (isNull(group)) {
            return Response.error(404, Map.of("message", "群不存在"));
        }

        var reason = ctx.formParam("reason");
        var reasonText = (reason != null && !reason.isEmpty()) ? reason : "未说明";

        // 处理待审核修改的拒绝
        if (group.pendingEdit != null && !group.pendingEdit.isEmpty() && !group.pending) {
            group.pendingEdit = null;
            PersistenceManager.INSTANCE.save();

            var content = String.format("您对群组「%s」的修改未通过审核\n原因: %s", group.groupName, reasonText);
            var applicant = group.getUser();
            if (applicant != null) {
                NotifyUtil.MAIL.send("Linktofur.net - 修改审核未通过通知", content, applicant);
            }

            log.info("Group {} edit rejected by {}", group.groupName, user.name);
            return Response.success(Map.of("message", "已拒绝修改"));
        }

        if (!group.pending) {
            return Response.error(400, Map.of("message", "该群已审核通过 无法拒绝"));
        }

        var content = String.format("您提交的群组「%s」未通过审核\n原因: %s", group.groupName, reasonText);
        var applicant = group.getUser();
        if (applicant != null) {
            NotifyUtil.MAIL.send("Linktofur.net - 审核未通过通知", content, applicant);
        }

        GroupManager.INSTANCE.removeGroup(groupId);

        log.info("Group {} rejected by {}", group.groupName, user.name);

        return Response.success(Map.of("message", "已拒绝"));
    }
}
