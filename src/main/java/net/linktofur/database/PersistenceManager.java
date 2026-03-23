package net.linktofur.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.group.GroupManager;
import net.linktofur.user.UserManager;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2026/3/23
 */
@Slf4j
public class PersistenceManager {
    public static final PersistenceManager INSTANCE = new PersistenceManager();
    private static final File dbFile = new File("data.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public void load() {
        if (!dbFile.exists()) {
            log.info("data.json not found, starting with empty data.");
            return;
        }
        try {
            Data data = mapper.readValue(dbFile, Data.class);
            if (data.users != null) {
                UserManager.INSTANCE.users = new ConcurrentHashMap<>(data.users);
            }
            if (data.groups != null) {
                GroupManager.INSTANCE.groups = new ConcurrentHashMap<>(data.groups);
            }
            log.info("Loaded {} users and {} groups from data.json", UserManager.INSTANCE.users.size(), GroupManager.INSTANCE.groups.size());
        } catch (Exception e) {
            log.error("Failed to load data.json", e);
        }
    }

    public synchronized void save() {
        try {
            if (!dbFile.exists()) {
                if (!dbFile.createNewFile()) {
                    throw new RuntimeException("Failed to create data.json file.");
                }
            }
            Data data = new Data();
            data.users = UserManager.INSTANCE.users;
            data.groups = GroupManager.INSTANCE.groups;
            mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, data);
            log.info("Saved data to data.json");
        } catch (Exception e) {
            log.error("Failed to save data.json", e);
        }
    }
}