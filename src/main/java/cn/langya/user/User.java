package cn.langya.user;

import lombok.Builder;

import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@Builder
public class User {
    public UUID id;
    public String name;
    public String email;
    public String password;
    public UserLevel level;

    @Builder.Default
    public boolean verified = false;
    @Builder.Default
    private String verifyCode = null;
    @Builder.Default
    public long lastLogin = 0;
    @Builder.Default
    public long lastLogout = 0;

    public String getVerifyCode() {
        if (verifyCode == null) {
            verifyCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        }
        return verifyCode;
    }

    public boolean isAdmin() {
        return level == UserLevel.ADMIN;
    }
}
