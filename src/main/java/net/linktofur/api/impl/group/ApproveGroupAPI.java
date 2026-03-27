package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.Main;
import net.linktofur.database.PersistenceManager;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;
import net.linktofur.util.NotifyUtil;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class ApproveGroupAPI extends API {
    public ApproveGroupAPI() {
        super("group/approve");
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

        // 处理待审核修改
        if (group.pendingEdit != null && !group.pendingEdit.isEmpty()) {
            var edit = group.pendingEdit;
            if (edit.containsKey("groupId")) group.groupId = edit.get("groupId");
            if (edit.containsKey("groupName")) group.groupName = edit.get("groupName");
            if (edit.containsKey("orgName")) group.orgName = edit.get("orgName");
            if (edit.containsKey("region")) group.region = edit.get("region");
            if (edit.containsKey("joinEntry")) group.joinEntry = edit.get("joinEntry");
            if (edit.containsKey("type")) {
                var groupType = GroupType.parse(edit.get("type"));
                if (groupType != null) group.type = groupType;
            }
            group.pendingEdit = null;

            // 如果群本身不是pending 说明这是修改审核 不是新群审核
            if (!group.pending) {
                PersistenceManager.INSTANCE.save();
                log.info("Group {} edit approved by {}", group.groupName, user.name);

                var content = String.format("您对群组「%s」的修改已通过审核", group.groupName);
                var applicant = group.getUser();
                if (applicant != null) {
                    NotifyUtil.MAIL.send("Linktofur.net - 修改审核通过通知", content, applicant);
                }
                return Response.success(Map.of("message", "修改审核通过"));
            }
        }

        if (!group.pending) {
            return Response.error(400, Map.of("message", "该群已审核通过"));
        }

        group.pending = false;
        PersistenceManager.INSTANCE.save();

        log.info("Group {} approved by {}", group.groupName, user.name);

        var content = String.format(
                "您提交的群组「%s」已通过审核 现已公开显示在 %s",
                group.groupName, Main.url
        );
        var applicant = group.getUser();
        if (applicant != null) {
            NotifyUtil.MAIL.send("Linktofur.net - 审核通过通知", content, applicant);
        }

        return Response.success(Map.of("message", "审核通过"));
    }
}