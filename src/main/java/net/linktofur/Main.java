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
import java.util.HashMap;
import java.util.Map;

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

                cfg.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
                    rule.anyHost();
                    rule.exposeHeader("Authorization");
                }));

                cfg.routes.before(ctx -> {
                    ctx.header("X-Content-Type-Options", "nosniff");
                    ctx.header("X-Frame-Options", "DENY");
                    ctx.header("Referrer-Policy", "no-referrer");
                    ctx.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                });

                for (API api : APIManager.INSTANCE.apis) {
                    Handler handler = ctx -> {
                        try {
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

            log.info("Listening on {}:{}", bindHost, port);

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