package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.Main;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.Group;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;
import net.linktofur.util.NotifyUtil;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@Slf4j
@SuppressWarnings({"DataFlowIssue", "unused"})
public class AddGroupAPI extends API {
    public AddGroupAPI() {
        super("group/add");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        var groupId = ctx.formParam("groupId");
        var groupName = ctx.formParam("groupName");
        var orgName = ctx.formParam("orgName");
        var region = ctx.formParam("region");
        var type = ctx.formParam("type");
        var joinEntry = ctx.formParam("joinEntry");

        if (isNull(groupId, groupName, orgName, region, type, joinEntry)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var groupType = GroupType.parse(type);
        if (groupType == null) {
            return Response.error(400, Map.of("message", "无效的群类型"));
        }

        var group = Group.builder()
                .groupId(groupId).groupName(groupName).orgName(orgName)
                .userId(user.id).region(region).type(groupType).joinEntry(joinEntry)
                .pending(true)
                .build();
        GroupManager.INSTANCE.addGroup(group);

        var typeName = groupType == GroupType.SCHOOL ? "院校群" : "地区联合群";
        var roleTag = user.isAdmin() ? "管理员" : "普通用户";
        var reviewUrl = Main.url + "?review=" + group.id;
        var message = String.format(
                "审核请求\n提交者: %s（%s）\n邮箱: %s\n身份: %s\n\n群名: %s\n组织: %s\n地区: %s\n类型: %s\n加群方式: %s\n审核链接: %s",
                user.name, roleTag, user.email, roleTag, groupName, orgName, region, typeName, joinEntry, reviewUrl
        );
        NotifyUtil.BOT.send(message);

        return Response.success(Map.of("message", "已提交审核"));
    }
}
