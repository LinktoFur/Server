package net.linktofur.api.impl.group;

import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.captcha.CaptchaManager;
import net.linktofur.group.GroupManager;
import net.linktofur.user.User;
import net.linktofur.util.NotifyUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author LangYa466
 * @date 2026/3/27
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class ApplyGroupAPI extends API {
    private static final Pattern QQ_PATTERN = Pattern.compile("^[1-9][0-9]{4,12}$");
    private static final int STATEMENT_MAX = 500;
    private static final int OTHER_CONTACT_MAX = 100;
    private static final long COOLDOWN_MS = 5 * 60 * 1000L;
    private static final ConcurrentHashMap<String, Long> recentApplies = new ConcurrentHashMap<>();

    public ApplyGroupAPI() {
        super("group/apply");
    }

    @Override
    public Response run(Context ctx) {
        var captchaId = ctx.formParam("captchaId");
        var captchaAnswer = ctx.formParam("captchaAnswer");

        if (!CaptchaManager.INSTANCE.validate(captchaId, captchaAnswer)) {
            return Response.error(400, Map.of("message", "人机验证失败"));
        }

        var idStr = ctx.formParam("id");
        var qq = ctx.formParam("qq");
        var statement = ctx.formParam("statement");
        var otherContact = ctx.formParam("otherContact");

        if (isNull(idStr, qq, statement)) {
            return Response.error(400, Map.of("message", "请填写QQ号和证明信息"));
        }

        if (!QQ_PATTERN.matcher(qq).matches()) {
            return Response.error(400, Map.of("message", "QQ号格式不正确"));
        }
        if (statement.length() > STATEMENT_MAX) {
            return Response.error(400, Map.of("message", "申请说明过长"));
        }
        if (otherContact != null && otherContact.length() > OTHER_CONTACT_MAX) {
            return Response.error(400, Map.of("message", "联系方式过长"));
        }

        int groupId;
        try {
            groupId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return Response.error(400, Map.of("message", "参数有问题"));
        }

        var group = GroupManager.INSTANCE.getGroupById(groupId);

        if (isNull(group) || group.pending) {
            return Response.error(404, Map.of("message", "群不存在"));
        }

        if (!group.acceptApply) {
            return Response.error(403, Map.of("message", "该群组暂不接收申请"));
        }

        User owner = group.getUser();
        if (owner == null) {
            return Response.error(500, Map.of("message", "该群组暂无联系人"));
        }

        // 同一 (groupId, qq) 5 分钟冷却 防止刷邮件骚扰群主
        var rateKey = groupId + ":" + qq;
        var now = System.currentTimeMillis();
        var prev = recentApplies.get(rateKey);
        if (prev != null && now - prev < COOLDOWN_MS) {
            return Response.error(429, Map.of("message", "申请过于频繁 请稍后再试"));
        }
        recentApplies.put(rateKey, now);
        if (recentApplies.size() > 10000) {
            recentApplies.entrySet().removeIf(e -> now - e.getValue() > COOLDOWN_MS);
        }

        var safeGroupName = NotifyUtil.escapeHtml(group.groupName);
        var safeQq = NotifyUtil.escapeHtml(qq);
        var safeStatement = NotifyUtil.escapeHtml(statement);
        var safeOtherContact = NotifyUtil.escapeHtml(otherContact);

        var sb = new StringBuilder();
        sb.append("<h3>有人申请加入您的群组「").append(safeGroupName).append("」</h3>");
        sb.append("<p><b>申请者QQ：</b>").append(safeQq).append("</p>");
        sb.append("<p><b>申请说明：</b>").append(safeStatement).append("</p>");
        if (otherContact != null && !otherContact.isEmpty()) {
            sb.append("<p><b>其他联系方式：</b>").append(safeOtherContact).append("</p>");
        }
        sb.append("<hr>");
        sb.append("<p style='color:#888;font-size:12px;'>Linktofur提醒您 如果申请者的QQ搜不到");
        if (otherContact != null && !otherContact.isEmpty()) {
            sb.append("可以通过其提供的其他联系方式联系 或者");
        }
        sb.append("可以直接通过QQ邮件联系他(").append(safeQq).append("@qq.com)</p>");

        NotifyUtil.MAIL.send("Linktofur.net - 加群申请通知", sb.toString(), owner);

        log.info("Group apply: qq={} -> group={} owner={}", qq, group.groupName, owner.name);

        return Response.success(Map.of("message", "申请已发送 请等待组织负责人联系您"));
    }
}
