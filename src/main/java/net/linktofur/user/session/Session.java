package net.linktofur.user.session;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
@AllArgsConstructor
@NoArgsConstructor
public class Session {
    public UUID userId;
    public long expireAt;
}