package cn.langya.user.session;

import lombok.extern.slf4j.Slf4j;

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
    private static Map<UUID, Session> sessions;
    private static final long SESSION_DURATION = 7 * 24 * 60 * 60 * 1000L; // 7d

    public SessionManager() {
        sessions = new ConcurrentHashMap<>();
    }

    public UUID createSession(UUID userId) {
        UUID sessionId = UUID.randomUUID();
        long expireAt = System.currentTimeMillis() + SESSION_DURATION;

        sessions.put(sessionId, new Session(userId, expireAt));
        return sessionId;
    }

    public UUID getUserId(UUID sessionId) {
        Session data = sessions.get(sessionId);

        if (data == null) return null;

        if (System.currentTimeMillis() > data.expireAt) {
            sessions.remove(sessionId);
            return null;
        }

        return data.userId;
    }

    public void removeSession(UUID sessionId) {
        sessions.remove(sessionId);
    }
}