package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.Main;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.group.Group;
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

        if (!user.isAdmin() && !user.id.equals(group.userId)) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        var edit = new HashMap<String, String>();
        for (var key : new String[]{"groupId", "groupName", "orgName", "region", "type", "joinEntry"}) {
            var val = ctx.formParam(key);
            if (val != null && !val.isEmpty()) edit.put(key, val);
        }

        for (var key : new String[]{"showContact", "acceptApply"}) {
            var val = ctx.formParam(key);
            if (val != null) edit.put(key, val);
        }

        if (edit.isEmpty()) {
            return Response.error(400, Map.of("message", "没有需要修改的内容"));
        }

        var changeLines = formatChanges(edit);

        if (user.isAdmin()) {
            // 管理员直接应用修改
            applyEdit(group, edit);
            group.pendingEdit = null;
            PersistenceManager.INSTANCE.save();
            log.info("Group {} edited directly by admin {}", group.groupName, user.name);

            NotifyUtil.BOT.send(String.format(
                    "群组信息已修改（管理员直接修改）\n操作者: %s（管理员）\n邮箱: %s\n\n修改内容:%s",
                    user.name, user.email, changeLines
            ));
            return Response.success(Map.of("message", "修改成功"));
        } else {
            // 普通用户提交审核
            group.pendingEdit = edit;
            PersistenceManager.INSTANCE.save();
            log.info("Group {} edit submitted for review by {}", group.groupName, user.name);

            var reviewUrl = Main.url + "?review=" + group.id;
            NotifyUtil.BOT.send(String.format(
                    "群组修改审核请求\n提交者: %s（用户）\n邮箱: %s\n修改内容:%s\n审核链接: %s",
                    user.name, user.email, changeLines, reviewUrl
            ));
            return Response.success(Map.of("message", "修改已提交审核"));
        }
    }

    private void applyEdit(Group group, Map<String, String> edit) {
        edit.forEach((key, val) -> {
            switch (key) {
                case "groupId" -> group.groupId = val;
                case "groupName" -> group.groupName = val;
                case "orgName" -> group.orgName = val;
                case "region" -> group.region = val;
                case "joinEntry" -> group.joinEntry = val;
                case "type" -> {
                    var parsed = GroupType.parse(val);
                    if (parsed != null) group.type = parsed;
                }
                case "showContact" -> group.showContact = Boolean.parseBoolean(val);
                case "acceptApply" -> group.acceptApply = Boolean.parseBoolean(val);
            }
        });
    }

    private static final Map<String, String> EN_TO_CN = Map.ofEntries(
            Map.entry("groupId", "群号"),
            Map.entry("groupName", "群名"),
            Map.entry("orgName", "组织"),
            Map.entry("region", "地区"),
            Map.entry("type", "类型"),
            Map.entry("joinEntry", "加群方式"),
            Map.entry("showContact", "展示联系方式"),
            Map.entry("acceptApply", "接收申请")
    );

    private String formatChanges(Map<String, String> edit) {
        var sb = new StringBuilder();
        edit.forEach((k, v) -> {
            var label = EN_TO_CN.getOrDefault(k, k);
            var display = "type".equals(k) ? ("SCHOOL".equals(v) ? "院校群" : "地区联合群") : v;
            sb.append(String.format("\n  %s: %s", label, display));
        });
        return sb.toString();
    }
}