package net.linktofur.group;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
public enum GroupType {
    SCHOOL, // 学校群
    REGION; // 地区联合群

    public static GroupType parse(String value) {
        try {
            return GroupType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
