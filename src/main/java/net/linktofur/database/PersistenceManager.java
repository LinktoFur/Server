package net.linktofur.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.group.GroupManager;
import net.linktofur.user.UserManager;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author LangYa466
 * @date 2026/3/23
 */
@Slf4j
public class PersistenceManager {
    public static final PersistenceManager INSTANCE = new PersistenceManager();
    private static final File dbFile = new File("data/data.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        var t = new Thread(r, "auto-save");
        t.setDaemon(true);
        return t;
    });

    public void load() {
        log.info("Data file path: {}", dbFile.getAbsolutePath());

        if (!dbFile.exists()) {
            log.info("data.json not found, starting with empty data.");
            ensureFileExists();
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
            GroupManager.INSTANCE.syncNextId();
            log.info("Loaded {} users and {} groups from data.json", UserManager.INSTANCE.users.size(), GroupManager.INSTANCE.groups.size());
        } catch (Exception e) {
            log.error("Failed to load data.json", e);
        }
    }

    public void startAutoSave() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                save();
            } catch (Exception e) {
                log.error("Auto save failed", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
        log.info("Auto save started (every 5 seconds)");
    }

    private void ensureFileExists() {
        try {
            var parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                var created = parent.mkdirs();
                log.info("Created data directory: {} success={}", parent.getAbsolutePath(), created);
            }
            if (!dbFile.exists()) {
                var created = dbFile.createNewFile();
                log.info("Created data file: {} success={}", dbFile.getAbsolutePath(), created);
                mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, new Data());
            }
        } catch (Exception e) {
            log.error("Failed to create data file at {}", dbFile.getAbsolutePath(), e);
        }
    }

    public synchronized void save() {
        try {
            ensureFileExists();
            Data data = new Data();
            data.users = UserManager.INSTANCE.users;
            data.groups = GroupManager.INSTANCE.groups;
            mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, data);
        } catch (Exception e) {
            log.error("Failed to save data.json at {}", dbFile.getAbsolutePath(), e);
        }
    }
}