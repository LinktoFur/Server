package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.GroupManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@SuppressWarnings("unused")
public class DeleteGroupAPI extends API {
    public DeleteGroupAPI() {
        super("group/delete");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }

        if (!user.isAdmin()) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        String groupId = ctx.formParam("groupId");

        if (isNull(groupId)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        GroupManager.INSTANCE.removeGroup(groupId);
        return Response.success(Map.of("message", "删除成功"));
    }
}
