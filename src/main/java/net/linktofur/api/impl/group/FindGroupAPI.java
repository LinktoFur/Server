package net.linktofur.api.impl.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.Group;
import net.linktofur.group.GroupManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/22
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
public class FindGroupAPI extends API {
    private final ObjectMapper MAPPER = new ObjectMapper();

    public FindGroupAPI() {
        super("group/find");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return Response.error(401, Map.of("message", "未登入"));
        }

        var content = ctx.formParam("content");

        if (isNull(content)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        List<Group> groups = new ArrayList<>();
        GroupManager.INSTANCE.groups.values().forEach(group -> {
            if (group.groupName.contains(content) || group.region.contains(content)) {
                groups.add(group);
            }
        });

        if (isNull(groups)) {
            return Response.error(404, Map.of("message", "群不存在"));
        }

        return Response.success(Map.of(
                "message", "查询成功",
                "groups", MAPPER.writeValueAsString(groups)
        ));
    }
}
