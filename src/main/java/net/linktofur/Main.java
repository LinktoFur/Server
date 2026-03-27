package net.linktofur;

import io.javalin.Javalin;

import io.javalin.http.Handler;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.API;
import net.linktofur.api.APIManager;
import net.linktofur.api.Response;
import net.linktofur.database.PersistenceManager;

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

        // 先这样吧 能work就行
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
            PersistenceManager.INSTANCE.startAutoSave();

            app = Javalin.create(config -> {
                // CORS
                config.routes.before(ctx -> {
                    ctx.header("Access-Control-Allow-Origin", "*");
                    ctx.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
                });
                config.routes.options("/*", ctx -> ctx.status(204));

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
                    config.routes.get(api.name, handler);
                    config.routes.post(api.name, handler);
                    log.info("Registered API: {}", api.name);
                }
                log.info("All APIs have been registered.");
            }).start(2778);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down... saving data.");
                PersistenceManager.INSTANCE.save();
            }));

        } catch (Exception e) {
            log.error("Main execution failed", e);
        }
    }
}