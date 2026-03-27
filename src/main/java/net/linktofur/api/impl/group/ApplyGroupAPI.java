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

/**
 * @author LangYa466
 * @date 2026/3/27
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Slf4j
public class ApplyGroupAPI extends API {
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

        User owner = group.getUser();
        if (owner == null) {
            return Response.error(500, Map.of("message", "该群组暂无联系人"));
        }

        var sb = new StringBuilder();
        sb.append("<h3>有人申请加入您的群组「").append(group.groupName).append("」</h3>");
        sb.append("<p><b>申请者QQ：</b>").append(qq).append("</p>");
        sb.append("<p><b>申请说明：</b>").append(statement).append("</p>");
        if (otherContact != null && !otherContact.isEmpty()) {
            sb.append("<p><b>其他联系方式：</b>").append(otherContact).append("</p>");
        }
        sb.append("<hr>");
        sb.append("<p style='color:#888;font-size:12px;'>Linktofur提醒您 如果申请者的QQ搜不到");
        if (otherContact != null && !otherContact.isEmpty()) {
            sb.append("可以通过其提供的其他联系方式联系 或者");
        }
        sb.append("可以直接通过QQ邮件联系他(").append(qq).append("@qq.com)</p>");

        NotifyUtil.MAIL.send("Linktofur.net - 加群申请通知", sb.toString(), owner);

        log.info("Group apply: qq={} -> group={} owner={}", qq, group.groupName, owner.name);

        return Response.success(Map.of("message", "申请已发送 请等待组织负责人联系您"));
    }
}
