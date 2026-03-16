package net.linktofur.group;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
public class GroupManager {
    public static final GroupManager INSTANCE = new GroupManager();
    public Map<String, Group> groups;

    public GroupManager() {
        groups = new ConcurrentHashMap<>();
    }

    public void addGroup(Group group) {
        groups.put(group.groupId, group);
    }

    public Group getGroupById(String id) {
        return groups.get(id);
    }

    public List<Group> getGroupsByType(GroupType type) {
        return groups.values().stream()
                .filter(group -> group.type == type)
                .toList();
    }

    public List<Group> getGroupsByRegion(String region) {
        return groups.values().stream()
                .filter(group -> group.region.equals(region))
                .toList();
    }

    public void removeGroup(String id) {
        groups.remove(id);
    }
}