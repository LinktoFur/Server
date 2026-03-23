package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.Group;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
public class AddGroupAPI extends API {
    public AddGroupAPI() {
        super("group/add");
    }

    @Override
    public Response run(Context ctx) {
        var user = getUser(ctx);

        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }

        if (!user.isAdmin()) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        var groupId = ctx.formParam("groupId");
        var groupName = ctx.formParam("groupName"); // 考虑到组织联合群 所以改成groupName而非schoolName
        var orgName = ctx.formParam("orgName");
        var region = ctx.formParam("region"); // 提交格式 广东 新疆 四川 哈尔滨 不用带后缀 如果是海外 就填国家就行 比如 美国 英国 日本 爱沙尼亚
        var type = ctx.formParam("type"); // REGION OR SCHOOL
        var joinEntry = ctx.formParam("joinEntry"); // 就是展示的 可以是加群问卷 可以是邮箱 也可以直接是群号 默认的话是传群号 记得在前端写好说明

        if (isNull(groupId, groupName, orgName, region, type, joinEntry)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var groupType = GroupType.parse(type);
        if (groupType == null) {
            return Response.error(400, Map.of("message", "无效的群类型"));
        }

        var group = Group.builder().groupId(groupId).groupName(groupName).user(user).region(region).type(groupType).joinEntry(joinEntry).build();
        GroupManager.INSTANCE.addGroup(group);

        return Response.success(Map.of("message", "添加成功"));
    }
}
