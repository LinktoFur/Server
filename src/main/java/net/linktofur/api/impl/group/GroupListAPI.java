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
public class GroupListAPI extends API {
    private final ObjectMapper MAPPER = new ObjectMapper();

    public GroupListAPI() {
        super("group/list");
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var user = getUser(ctx);

        if (isNull(user)) {
            return authError(ctx);
        }

        // 已审核的群组 + 自己提交的待审核群组
        var groups = GroupManager.INSTANCE.groups.values().stream()
                .filter(g -> !g.pending || (g.userId != null && g.userId.equals(user.id)))
                .map(g -> {
                    var map = new HashMap<String, String>();
                    map.put("id", String.valueOf(g.id));
                    map.put("groupId", g.groupId);
                    map.put("groupName", g.groupName);
                    map.put("orgName", g.orgName != null ? g.orgName : "");
                    map.put("region", g.region);
                    map.put("type", g.type.name());
                    map.put("joinEntry", g.joinEntry);
                    map.put("userId", g.userId != null ? g.userId.toString() : "");
                    if (user.isAdmin()) {
                        User owner = g.getUser();
                        map.put("userName", owner != null ? owner.name : "");
                    }
                    map.put("pending", String.valueOf(g.pending));
                    map.put("showContact", String.valueOf(g.showContact));
                    map.put("acceptApply", String.valueOf(g.acceptApply));
                    map.put("hasPendingEdit", String.valueOf(g.pendingEdit != null && !g.pendingEdit.isEmpty()));
                    map.put("createdAt", String.valueOf(g.createdAt));
                    return map;
                })
                .toList();

        return Response.success(Map.of(
                "message", "查询成功",
                "total", String.valueOf(groups.size()),
                "groups", MAPPER.writeValueAsString(groups)
        ));
    }
}