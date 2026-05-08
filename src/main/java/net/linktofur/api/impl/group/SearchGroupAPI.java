package net.linktofur.api.impl.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.group.GroupManager;
import net.linktofur.group.GroupType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
public class SearchGroupAPI extends API {
    private final ObjectMapper MAPPER = new ObjectMapper();

    public SearchGroupAPI() {
        super("group/search", true);
    }

    @Override
    public Response run(Context ctx) throws Exception {
        var rawContent = ctx.queryParam("content");
        var type = ctx.queryParam("type"); // SCHOOL or REGION

        if (isNull(rawContent)) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }
        var content = rawContent.trim();
        if (content.length() < 2) {
            return Response.error(400, Map.of("message", "请输入至少 2 个字"));
        }
        if (content.length() > 64) {
            return Response.error(400, Map.of("message", "搜索内容过长"));
        }

        GroupType filterType = null;
        if (type != null && !type.isEmpty()) {
            filterType = GroupType.parse(type);
        }

        List<Map<String, String>> results = new ArrayList<>();
        GroupType finalFilterType = filterType;

        GroupManager.INSTANCE.groups.values().forEach(group -> {
            if (group.pending) return; // 过滤待审核
            if (finalFilterType != null && group.type != finalFilterType) return;

            var nameMatch = group.groupName != null && group.groupName.contains(content);
            var regionMatch = group.region != null && group.region.contains(content);
            if (nameMatch || regionMatch) {
                if (group.type == GroupType.SCHOOL) {
                    results.add(Map.of(
                            "id", String.valueOf(group.id),
                            "school", group.groupName,
                            "org", group.groupName,
                            "region", group.region
                    ));
                } else {
                    results.add(Map.of(
                            "id", String.valueOf(group.id),
                            "org", group.groupName,
                            "intro", group.region + "地区联合群"
                    ));
                }
            }
        });

        return Response.success(Map.of(
                "message", "查询成功",
                "total", String.valueOf(results.size()),
                "results", MAPPER.writeValueAsString(results)
        ));
    }
}