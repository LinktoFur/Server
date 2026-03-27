package net.linktofur.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import net.linktofur.user.User;
import net.linktofur.user.UserManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    public String groupName;
    public String orgName;
    public UUID userId; // 只存用户ID 不存整个User对象
    public GroupType type;
    public String groupId;
    public String joinEntry;
    public String region;
    @Builder.Default
    public boolean pending = false;
    @Builder.Default
    public int id = 0;

    @Builder.Default
    public long createdAt = System.currentTimeMillis();

    // 待审核的修改内容 null表示没有待审核修改
    public Map<String, String> pendingEdit;

    @JsonIgnore
    public User getUser() {
        if (userId == null) return null;
        return UserManager.INSTANCE.getUserById(userId);
    }

    @JsonIgnore
    public boolean isSchool() {
        return type == GroupType.SCHOOL;
    }
}
