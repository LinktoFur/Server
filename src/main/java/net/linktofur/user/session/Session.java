package net.linktofur.user.session;

import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
@AllArgsConstructor
public class Session {
    public UUID userId;
    public Instant expireAt;
}