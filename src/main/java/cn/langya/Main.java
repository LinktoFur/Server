package cn.langya;

import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import cn.langya.api.API;
import cn.langya.api.APIManager;
import cn.langya.api.Response;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@Slf4j
public class Main {
    public static Javalin app;

    public static void main(String[] args) {
        try {
            log.info("Starting application...");

            app = Javalin.create(config -> {
                for (API api : APIManager.INSTANCE.apis) {
                    config.routes.get(api.name, ctx -> {
                        try {
                            Response response = api.run(ctx);
                            ctx.status(response.code).json(response);
                        } catch (Exception e) {
                            log.error("API {} execution failed", api.name, e);
                            ctx.status(500).json(Response.error(500, Map.of("message", "服务器错误")));
                        }
                    });
                    log.info("Registered API: {}", api.name);
                }
                log.info("All APIs have been registered.");
            }).start(2778);

        } catch (Exception e) {
            log.error("Main execution failed", e);
        }
    }
}