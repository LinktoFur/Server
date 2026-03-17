package net.linktofur.api.impl;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@SuppressWarnings("unused")
public class TestAPI extends API {
    public TestAPI() {
        super("/");
    }

    @Override
    public Response run(Context ctx) {
        return Response.success(Map.of("running", "ok"));
    }
}