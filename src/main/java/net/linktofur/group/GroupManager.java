package net.linktofur.group;

import net.linktofur.database.PersistenceManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
public class GroupManager {
    public static final GroupManager INSTANCE = new GroupManager();
    public Map<Integer, Group> groups;

    public GroupManager() {
        groups = new ConcurrentHashMap<>();
    }

    public void addGroup(Group group) {
        group.id = groups.size() + 1;
        groups.put(group.id, group);
        PersistenceManager.INSTANCE.save();
    }

    public Group getGroupById(Integer id) {
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

    public void removeGroup(Integer id) {
        groups.remove(id);
        PersistenceManager.INSTANCE.save();
    }
}