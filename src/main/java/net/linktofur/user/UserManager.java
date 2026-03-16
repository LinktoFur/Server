package net.linktofur.user;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
public class UserManager {
    public static final UserManager INSTANCE = new UserManager();
    public Map<UUID, User> users;

    public UserManager() {
        users = new ConcurrentHashMap<>();
    }

    public void addUser(User user) {
        if (users.isEmpty()) {
            user.level = UserLevel.ADMIN;
        }
        users.put(user.id, user);
    }

    public User getUserByEmail(String email) {
        return users.values().stream().filter(user -> user.email.equals(email)).findFirst().orElse(null);
    }

    public User getUserById(UUID userId) {
        return users.get(userId);
    }

    public void deleteUser(User user) {
        users.remove(user.id);
    }
}
