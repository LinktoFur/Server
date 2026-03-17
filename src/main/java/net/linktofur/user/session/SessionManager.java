package net.linktofur.user.session;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2026/2/28
 */
@Slf4j
public class SessionManager {
    public static final SessionManager INSTANCE = new SessionManager();
    private static final Duration SESSION_DURATION = Duration.ofDays(7);
    private final Map<UUID, Session> sessions;

    private SessionManager() {
        sessions = new ConcurrentHashMap<>();
    }

    public UUID createSession(UUID userId) {
        var sessionId = UUID.randomUUID();
        sessions.put(sessionId, new Session(userId, Instant.now().plus(SESSION_DURATION)));
        return sessionId;
    }

    public UUID getUserId(UUID sessionId) {
        var session = sessions.get(sessionId);
        if (session == null) return null;
        if (Instant.now().isAfter(session.expireAt)) {
            sessions.remove(sessionId);
            return null;
        }
        return session.userId;
    }

    public void removeSession(UUID sessionId) {
        sessions.remove(sessionId);
    }
}