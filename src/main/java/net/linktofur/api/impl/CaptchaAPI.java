package net.linktofur.api.impl;

import io.javalin.http.Context;
import net.linktofur.api.API;
import net.linktofur.api.Response;
import net.linktofur.captcha.CaptchaManager;

import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings("unused")
public class CaptchaAPI extends API {
    public CaptchaAPI() {
        super("captcha/generate");
    }

    @Override
    public Response run(Context ctx) {
        var result = CaptchaManager.INSTANCE.generate();

        if (result == null) {
            return Response.error(500, Map.of("message", "验证码生成失败"));
        }

        return Response.success(Map.of(
                "captchaId", result[0],
                "image", result[1]
        ));
    }
}