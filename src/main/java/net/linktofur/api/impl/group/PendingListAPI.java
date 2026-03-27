package net.linktofur.api.impl.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.GroupManager;
import net.linktofur.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings({"unused"})
@Slf4j
public class PendingListAPI extends API {
    private final ObjectMapper MAPPER = new ObjectMapper();

    public PendingListAPI() {
        super("group/pending");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        if (!user.isAdmin()) {
            return Response.error(403, Map.of("message", "权限不足"));
        }

        var pending = GroupManager.INSTANCE.groups.values().stream()
                .filter(g -> g.pending || (g.pendingEdit != null && !g.pendingEdit.isEmpty()))
                .map(g -> {
                    var map = new HashMap<String, Object>();
                    map.put("id", String.valueOf(g.id));
                    map.put("groupId", g.groupId);
                    map.put("groupName", g.groupName);
                    map.put("orgName", g.orgName != null ? g.orgName : "");
                    map.put("region", g.region);
                    map.put("type", g.type.name());
                    map.put("joinEntry", g.joinEntry);
                    map.put("createdAt", String.valueOf(g.createdAt));
                    map.put("userId", g.userId != null ? g.userId.toString() : "");
                    User owner = g.getUser();
                    map.put("userName", owner != null ? owner.name : "");
                    map.put("pendingType", g.pending ? "new" : "edit");
                    if (g.pendingEdit != null && !g.pendingEdit.isEmpty()) {
                        map.put("pendingEdit", g.pendingEdit);
                    }
                    return map;
                })
                .toList();

        return Response.success(Map.of(
                "message", "查询成功",
                "total", String.valueOf(pending.size()),
                "groups", MAPPER.writeValueAsString(pending)
        ));
    }
}
