package cn.langya.api.impl;

import io.javalin.http.Context;
import cn.langya.api.API;
import cn.langya.api.Response;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
public class TestAPI extends API {
    public TestAPI() {
        super("/");
    }

    @Override
    public Response run(Context ctx) {
        return Response.success(Map.of("running", "ok"));
    }
}