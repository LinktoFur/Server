package net.linktofur.group;

import lombok.*;
import net.linktofur.user.User;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    public String groupName;
    public User user;
    public GroupType type;
    public String groupId;
    public String joinEntry;
    public String region;
    @Builder.Default
    public int id = 0;

    @Builder.Default
    public long createdAt = System.currentTimeMillis();

    public boolean isSchool() {
        return type == GroupType.SCHOOL;
    }
}