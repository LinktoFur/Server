package net.linktofur.user.session;

import lombok.extern.slf4j.Slf4j;

import net.linktofur.database.PersistenceManager;

import java.time.Duration;
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
    private static final Duration duration = Duration.ofDays(30);
    public Map<UUID, Session> sessions;

    private SessionManager() {
        sessions = new ConcurrentHashMap<>();
    }

    public UUID createSession(UUID userId) {
        var sessionId = UUID.randomUUID();
        sessions.put(sessionId, new Session(userId, System.currentTimeMillis() + duration.toMillis()));
        PersistenceManager.INSTANCE.save();
        return sessionId;
    }

    public UUID getUserId(UUID sessionId) {
        var session = sessions.get(sessionId);
        if (session == null) return null;
        if (System.currentTimeMillis() > session.expireAt) {
            sessions.remove(sessionId);
            PersistenceManager.INSTANCE.save();
            return null;
        }
        return session.userId;
    }

    public void removeSession(UUID sessionId) {
        sessions.remove(sessionId);
        PersistenceManager.INSTANCE.save();
    }
}