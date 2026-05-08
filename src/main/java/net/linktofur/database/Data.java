package net.linktofur.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.linktofur.group.Group;
import net.linktofur.user.User;

import java.util.Map;
import java.util.UUID;

/**
 * @author LangYa466
 * @date 2026/3/23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
    public Map<UUID, User> users;
    public Map<Integer, Group> groups;
}