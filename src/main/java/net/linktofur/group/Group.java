package net.linktofur.group;

import lombok.Builder;
import net.linktofur.user.User;
import net.linktofur.util.NotifyUtil;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@Builder
public class Group {
    public String groupName;
    public User user;
    public GroupType type;
    public String groupId;
    public String region;
    @Builder.Default
    public int id = 0;

    @Builder.Default
    public long createdAt = System.currentTimeMillis();

    public boolean isSchool() {
        return type == GroupType.SCHOOL;
    }

    public void sendGroupInfoToQQ(User user) throws Exception {
        NotifyUtil.send(String.format("Linktofur.net - %s信息申请", isSchool() ? "学校交流群" : "地区联合群"),
                String.format("群名称: %s\n 群管理员: %s\n 群号(QQ): %s", groupName, user.name, groupId),
                user);
    }
}