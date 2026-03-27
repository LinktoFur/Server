package net.linktofur.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public UUID id;
    public String name;
    public String email;
    public UserType level;

    @Builder.Default
    public boolean banned = false;
    @Builder.Default
    public boolean verified = false;
    @Builder.Default
    public String verifyCode = null;
    @Builder.Default
    public long loginAt = 0;
    @Builder.Default
    public long logoutAt = 0;
    @Builder.Default
    public long createdAt = System.currentTimeMillis();

    public String getVerifyCode() {
        if (verifyCode == null) {
            verifyCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        }
        return verifyCode;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return level == UserType.ADMIN;
    }
}
