package net.linktofur.api.impl.admin;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.GroupManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@SuppressWarnings({"unused", "DataFlowIssue"})
public class DeleteGroupAPI extends API {
    public DeleteGroupAPI() {
        super("admin/deletegroup");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        // 管理员才能删除
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

        GroupManager.INSTANCE.removeGroup(groupId);
        return Response.success(Map.of("message", "删除成功"));
    }
}
