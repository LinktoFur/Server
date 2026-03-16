package net.linktofur.util;

import net.linktofur.user.User;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
public class NotifyUtil {
    public static void send(String title, String text, String email) throws Exception {
    }

    public static void send(String title, String text, User user) throws Exception {
        send(title, text, user.email);
    }
}