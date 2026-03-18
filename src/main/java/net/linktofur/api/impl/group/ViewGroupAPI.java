package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.GroupManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/17
 */
@SuppressWarnings({"unused", "DataFlowIssue"})
public class ViewGroupAPI extends API {
    public ViewGroupAPI() {
        super("group/view");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }

        var id = ctx.formParam("id");

        if (isNull(id)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        Integer groupId = Integer.valueOf(id);
        var group = GroupManager.INSTANCE.getGroupById(groupId);

        if (group == null) {
            return Response.error(404, Map.of("message", "没这个群"));
        }

        /*
        if (user.isAdmin()) {
            group.sendGroupInfoToQQ(user);
            return Response.error(403, Map.of("message", "提交成功 鉴于你权限并非普通用户 已通过QQ发送结果"));
        }

         */

        return Response.success(Map.of("message", "提交成功 结果会通过你绑定的QQ发送结果"));
    }
}
