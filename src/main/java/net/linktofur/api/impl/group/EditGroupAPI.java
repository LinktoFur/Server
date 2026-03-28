package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.Main;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;
import net.linktofur.util.NotifyUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/27
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class EditGroupAPI extends API {
    public EditGroupAPI() {
        super("group/edit");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
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

        // 只有群主或管理员可以修改
        if (!user.isAdmin() && !user.id.equals(group.userId)) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        var groupIdStr = ctx.formParam("groupId");
        var groupName = ctx.formParam("groupName");
        var orgName = ctx.formParam("orgName");
        var region = ctx.formParam("region");
        var type = ctx.formParam("type");
        var joinEntry = ctx.formParam("joinEntry");

        var edit = new HashMap<String, String>();
        if (groupIdStr != null && !groupIdStr.isEmpty()) edit.put("groupId", groupIdStr);
        if (groupName != null && !groupName.isEmpty()) edit.put("groupName", groupName);
        if (orgName != null && !orgName.isEmpty()) edit.put("orgName", orgName);
        if (region != null && !region.isEmpty()) edit.put("region", region);
        if (joinEntry != null && !joinEntry.isEmpty()) edit.put("joinEntry", joinEntry);
        if (type != null && !type.isEmpty()) edit.put("type", type);

        if (edit.isEmpty()) {
            return Response.error(400, Map.of("message", "没有需要修改的内容"));
        }

        var changeLines = formatChanges(edit);
        var roleTag = user.isAdmin() ? "管理员" : "用户";

        if (user.isAdmin()) {
            if (groupIdStr != null && !groupIdStr.isEmpty()) group.groupId = groupIdStr;
            if (groupName != null && !groupName.isEmpty()) group.groupName = groupName;
            if (orgName != null && !orgName.isEmpty()) group.orgName = orgName;
            if (region != null && !region.isEmpty()) group.region = region;
            if (joinEntry != null && !joinEntry.isEmpty()) group.joinEntry = joinEntry;
            if (type != null && !type.isEmpty()) {
                var groupType = GroupType.parse(type);
                if (groupType != null) group.type = groupType;
            }
            group.pendingEdit = null;
            PersistenceManager.INSTANCE.save();
            log.info("Group {} edited directly by admin {}", group.groupName, user.name);

            var message = String.format(
                    "群组信息已修改（管理员直接修改）\n操作者: %s（管理员）\n邮箱: %s\n\n修改内容:%s",
                    user.name, user.email, changeLines
            );
            NotifyUtil.BOT.send(message);

            return Response.success(Map.of("message", "修改成功"));
        } else {
            group.pendingEdit = edit;
            PersistenceManager.INSTANCE.save();
            log.info("Group {} edit submitted for review by {}", group.groupName, user.name);

            var reviewUrl = Main.url + "?review=" + group.id;
            var message = String.format(
                    "群组修改审核请求\n提交者: %s（用户）\n邮箱: %s\n修改内容:%s\n审核链接: %s",
                    user.name, user.email, changeLines, reviewUrl
            );
            NotifyUtil.BOT.send(message);

            return Response.success(Map.of("message", "修改已提交审核"));
        }
    }

    private static final Map<String, String> en2cnMap = Map.of(
            "groupId", "群号",
            "groupName", "群名",
            "orgName", "组织",
            "region", "地区",
            "type", "类型",
            "joinEntry", "加群方式"
    );

    private String formatChanges(Map<String, String> edit) {
        var sb = new StringBuilder();
        edit.forEach((k, v) -> {
            var label = en2cnMap.getOrDefault(k, k);
            var display = "type".equals(k) ? ("SCHOOL".equals(v) ? "院校群" : "地区联合群") : v;
            sb.append(String.format("\n  %s: %s", label, display));
        });
        return sb.toString();
    }
}