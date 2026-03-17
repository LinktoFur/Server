package net.linktofur.group;

import lombok.Builder;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@Builder
public class Group {
    public String groupName;
    public String orgName;
    public GroupType type;
    public String groupId;
    public String region;

    @Builder.Default
    public long createdAt = System.currentTimeMillis();

    public boolean isSchool() {
        return type == GroupType.SCHOOL;
    }

    public boolean isRegion() {
        return type == GroupType.REGION;
    }
}