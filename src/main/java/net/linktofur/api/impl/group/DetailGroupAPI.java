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
 * @date 2026/3/26
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
public class DetailGroupAPI extends API {
    public DetailGroupAPI() {
        super("group/detail");
    }

    @Override
    public Response run(Context ctx) {
        var idStr = ctx.queryParam("id");

        if (isNull(idStr)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        Group group = GroupManager.INSTANCE.getGroupById(id);

        if (isNull(group) || group.pending) {
            return Response.error(404, Map.of("message", "群不存在", "found", "false"));
        }

        if (group.type == GroupType.SCHOOL) {
            return Response.success(Map.of(
                    "found", "true",
                    "groupName", group.groupName,
                    "orgName", group.orgName,
                    "region", group.region
            ));
        } else {
            return Response.success(Map.of(
                    "found", "true",
                    "groupName", group.groupName,
                    "orgName", group.orgName,
                    "intro", group.region + "地区联合群"
            ));
        }
    }
}
