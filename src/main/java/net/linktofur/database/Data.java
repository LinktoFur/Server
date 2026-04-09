package net.linktofur.database;

import net.linktofur.group.Group;
import net.linktofur.user.User;
import net.linktofur.user.session.Session;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/23
 */
public class Data {
    public Map<UUID, User> users;
    public Map<Integer, Group> groups;
    public Map<UUID, Session> sessions;
}