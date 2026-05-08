package net.linktofur.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Builder
@ToString(exclude = {"verifyCode", "email"})
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private static final SecureRandom RNG = new SecureRandom();
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LEN = 8;
    private static final long CODE_TTL_MS = 5 * 60 * 1000L;
    private static final int CODE_MAX_ATTEMPTS = 5;
    private static final long CODE_RESEND_COOLDOWN_MS = 60 * 1000L;

    public UUID id;
    public String name;
    public String email;
    public UserType level;

    @Builder.Default
    public boolean banned = false;
    @Builder.Default
    public boolean verified = false;
    @JsonIgnore
    @Builder.Default
    public String verifyCode = null;
    @JsonIgnore
    @Builder.Default
    public long verifyCodeIssuedAt = 0;
    @JsonIgnore
    @Builder.Default
    public int verifyAttempts = 0;
    @Builder.Default
    public long loginAt = 0;
    @Builder.Default
    public long logoutAt = 0;
    @Builder.Default
    public long createdAt = System.currentTimeMillis();

    @JsonIgnore
    public boolean isAdmin() {
        return level == UserType.ADMIN;
    }

    @JsonIgnore
    public boolean canIssueCode() {
        return System.currentTimeMillis() - verifyCodeIssuedAt >= CODE_RESEND_COOLDOWN_MS;
    }

    public String issueVerifyCode() {
        var sb = new StringBuilder(CODE_LEN);
        for (int i = 0; i < CODE_LEN; i++) sb.append(CODE_ALPHABET[RNG.nextInt(CODE_ALPHABET.length)]);
        verifyCode = sb.toString();
        verifyCodeIssuedAt = System.currentTimeMillis();
        verifyAttempts = 0;
        return verifyCode;
    }

    public ConsumeResult consumeVerifyCode(String input) {
        if (verifyCode == null) return ConsumeResult.MISSING;
        if (System.currentTimeMillis() - verifyCodeIssuedAt > CODE_TTL_MS) {
            invalidateVerifyCode();
            return ConsumeResult.EXPIRED;
        }
        if (verifyAttempts >= CODE_MAX_ATTEMPTS) {
            invalidateVerifyCode();
            return ConsumeResult.LOCKED;
        }
        if (!constantTimeEquals(verifyCode, input)) {
            verifyAttempts++;
            if (verifyAttempts >= CODE_MAX_ATTEMPTS) invalidateVerifyCode();
            return ConsumeResult.MISMATCH;
        }
        invalidateVerifyCode();
        return ConsumeResult.OK;
    }

    private void invalidateVerifyCode() {
        verifyCode = null;
        verifyCodeIssuedAt = 0;
        verifyAttempts = 0;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }

    public enum ConsumeResult { OK, MISMATCH, EXPIRED, LOCKED, MISSING }
}
