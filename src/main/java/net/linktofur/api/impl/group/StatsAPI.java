package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/4/8
 */
@SuppressWarnings("unused")
public class StatsAPI extends API {
    public StatsAPI() {
        super("group/stats");
    }

    @Override
    public Response run(Context ctx) {
        long school = GroupManager.INSTANCE.groups.values().stream()
                .filter(g -> !g.pending && g.type == GroupType.SCHOOL)
                .count();
        long region = GroupManager.INSTANCE.groups.values().stream()
                .filter(g -> !g.pending && g.type == GroupType.REGION)
                .count();
        return Response.success(Map.of(
                "message", "ok",
                "school", String.valueOf(school),
                "region", String.valueOf(region)
        ));
    }
}
