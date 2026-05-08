package net.linktofur;

import io.javalin.Javalin;

import io.javalin.http.Handler;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.APIManager;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;
import net.linktofur.user.UserManager;
import net.linktofur.user.UserType;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@Slf4j
public class Main {
    public static Javalin app;
    public static final String url = "https://www.linktofur.net/";
    private static final File config = new File("config.toml");
    public static final Map<String, String> configs = new HashMap<>();
    public static Set<String> allowedOrigins = Set.of();

    static {
        log.info("Starting application...");

        try {
            if (config.exists()) {
                String[] lines = Files.readString(config.toPath()).split("\\r?\\n");
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] split = line.split("=", 2);
                    if (split.length < 2) continue;
                    configs.put(split[0].trim(), split[1].trim());
                }
            } else {
                log.error("config.toml not found");
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Failed to read config", e);
            System.exit(-1);
        }

        var origins = configs.getOrDefault("allowedOrigins", "");
        allowedOrigins = origins.isBlank()
                ? Set.of()
                : new HashSet<>(Arrays.asList(origins.split("\\s*,\\s*")));
    }

    public static void main(String[] args) {
        try {
            PersistenceManager.INSTANCE.load();
            bootstrapAdmin();
            PersistenceManager.INSTANCE.startAutoSave();

            var bindHost = configs.getOrDefault("bindHost", "127.0.0.1");
            var port = Integer.parseInt(configs.getOrDefault("port", "2778"));

            app = Javalin.create(cfg -> {
                cfg.http.maxRequestSize = 1_000_000L; // 1MB

                cfg.routes.before(ctx -> {
                    var origin = ctx.header("Origin");
                    if (origin != null && allowedOrigins.contains(origin)) {
                        ctx.header("Access-Control-Allow-Origin", origin);
                        ctx.header("Vary", "Origin");
                        ctx.header("Access-Control-Allow-Credentials", "true");
                    }
                    ctx.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");

                    ctx.header("X-Content-Type-Options", "nosniff");
                    ctx.header("X-Frame-Options", "DENY");
                    ctx.header("Referrer-Policy", "no-referrer");
                    ctx.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                });
                cfg.routes.options("/*", ctx -> ctx.status(204));

                for (API api : APIManager.INSTANCE.apis) {
                    Handler handler = ctx -> {
                        try {
                            // 写操作:Origin 必须命中白名单 否则当 CSRF 拒绝
                            if (!api.readOnly) {
                                var origin = ctx.header("Origin");
                                if (origin == null || !allowedOrigins.contains(origin)) {
                                    ctx.status(403).json(Response.error(403, Map.of("message", "来源不允许")));
                                    return;
                                }
                            }
                            Response response = api.run(ctx);
                            ctx.status(response.code).json(response);
                        } catch (Exception e) {
                            log.error("API {} execution failed", api.name, e);
                            ctx.status(500).json(Response.error(500, Map.of("message", "服务器错误 如果你是正常访问出现该错误 请联系网站作者")));
                        }
                    };
                    if (api.readOnly) {
                        cfg.routes.get(api.name, handler);
                    } else {
                        cfg.routes.post(api.name, handler);
                    }
                    log.info("Registered API: {} ({})", api.name, api.readOnly ? "GET" : "POST");
                }
                log.info("All APIs have been registered.");
            }).start(bindHost, port);

            log.info("Listening on {}:{} (allowedOrigins={})", bindHost, port, allowedOrigins);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down... stopping server first to drain in-flight requests");
                try {
                    if (app != null) app.stop();
                } catch (Exception e) {
                    log.error("Error stopping server", e);
                }
                log.info("Saving data...");
                PersistenceManager.INSTANCE.save();
            }));

        } catch (Exception e) {
            log.error("Main execution failed", e);
        }
    }

    private static void bootstrapAdmin() {
        var bootstrapEmail = configs.get("bootstrapAdminEmail");
        if (bootstrapEmail == null || bootstrapEmail.isBlank()) {
            if (UserManager.INSTANCE.users.values().stream().noneMatch(u -> u.level == UserType.ADMIN)) {
                log.warn("No ADMIN user exists and bootstrapAdminEmail is not set in config.toml");
            }
            return;
        }
        var user = UserManager.INSTANCE.getUserByEmail(bootstrapEmail);
        if (user == null) {
            log.warn("bootstrapAdminEmail {} has no matching account yet", bootstrapEmail);
            return;
        }
        if (user.level != UserType.ADMIN) {
            user.level = UserType.ADMIN;
            PersistenceManager.INSTANCE.markDirty();
            log.info("bootstrap: promoted {} to ADMIN", bootstrapEmail);
        }
    }
}