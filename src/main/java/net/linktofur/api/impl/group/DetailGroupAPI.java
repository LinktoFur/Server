package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.Group;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;

import java.util.HashMap;
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

        var result = new HashMap<String, String>();
        result.put("found", "true");
        result.put("groupName", group.groupName);
        result.put("orgName", group.orgName);
        result.put("showContact", String.valueOf(group.showContact));
        result.put("acceptApply", String.valueOf(group.acceptApply));

        if (group.showContact) result.put("contact", group.joinEntry);

        if (group.type == GroupType.SCHOOL) {
            result.put("region", group.region);
        } else {
            result.put("intro", group.region + "地区联合群");
        }

        return Response.success(result);
    }
}
