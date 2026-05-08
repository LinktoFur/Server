package net.linktofur.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.group.GroupManager;
import net.linktofur.user.UserManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author LangYa466
 * @date 2026/3/23
 */
@Slf4j
public class PersistenceManager {
    public static final PersistenceManager INSTANCE = new PersistenceManager();
    private static final File dbFile = new File("data/data.json");
    private static final File tmpFile = new File("data/data.json.tmp");
    private static final File bakFile = new File("data/data.json.bak");
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        var t = new Thread(r, "auto-save");
        t.setDaemon(true);
        return t;
    });

    public void load() {
        log.info("Data file path: {}", dbFile.getAbsolutePath());

        if (!dbFile.exists()) {
            log.info("data.json not found, starting with empty data.");
            ensureDirExists();
            return;
        }
        try {
            Data data = mapper.readValue(dbFile, Data.class);
            applyData(data);
            log.info("Loaded {} users, {} groups from data.json",
                    UserManager.INSTANCE.users.size(), GroupManager.INSTANCE.groups.size());
            return;
        } catch (Exception e) {
            log.error("Failed to load data.json, attempting fallback to {}", bakFile.getName(), e);
        }

        // 主文件挂了 不要让 auto-save 用空 Data 把它覆盖 走 bak 兜底
        if (bakFile.exists()) {
            try {
                Data data = mapper.readValue(bakFile, Data.class);
                applyData(data);
                log.warn("Recovered from {}: {} users, {} groups", bakFile.getName(),
                        UserManager.INSTANCE.users.size(), GroupManager.INSTANCE.groups.size());
                return;
            } catch (Exception e) {
                log.error("Backup file also corrupted", e);
            }
        }
        // 死保 防止 auto-save 把脏数据覆盖到磁盘
        log.error("Data file unrecoverable, refusing to start to avoid data loss");
        System.exit(2);
    }

    private void applyData(Data data) {
        if (data.users != null) {
            UserManager.INSTANCE.users = new ConcurrentHashMap<>(data.users);
        }
        if (data.groups != null) {
            GroupManager.INSTANCE.groups = new ConcurrentHashMap<>(data.groups);
        }
        GroupManager.INSTANCE.syncNextId();
    }

    public void startAutoSave() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (dirty.compareAndSet(true, false)) save();
            } catch (Exception e) {
                dirty.set(true);
                log.error("Auto save failed", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
        log.info("Auto save started (every 5 seconds, dirty-check)");
    }

    public void markDirty() {
        dirty.set(true);
    }

    private void ensureDirExists() {
        var parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            var created = parent.mkdirs();
            log.info("Created data directory: {} success={}", parent.getAbsolutePath(), created);
        }
    }

    public synchronized void save() {
        try {
            ensureDirExists();
            Data data = new Data();
            data.users = UserManager.INSTANCE.users;
            data.groups = GroupManager.INSTANCE.groups;
            // sessions 不再落盘 重启即清空 减少 token 泄漏面

            mapper.writerWithDefaultPrettyPrinter().writeValue(tmpFile, data);

            if (dbFile.exists()) {
                try {
                    Files.copy(dbFile.toPath(), bakFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                } catch (Exception e) {
                    log.warn("Backup copy failed", e);
                }
            }

            try {
                Files.move(tmpFile.toPath(), dbFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception atomicFail) {
                Files.move(tmpFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            dirty.set(true);
            log.error("Failed to save data.json at {}", dbFile.getAbsolutePath(), e);
        }
    }
}