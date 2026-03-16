package net.linktofur.api;

import lombok.extern.slf4j.Slf4j;
import net.linktofur.util.ClassUtil;

import java.util.Objects;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@Slf4j
public class APIManager {
    public static final APIManager INSTANCE = new APIManager();
    public API[] apis;

    public APIManager() {
        var classes = ClassUtil.findSubClasses("net.linktofur.api.impl", API.class);

        apis = classes.stream()
                .map(clazz -> {
                    try {
                        return clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        log.error("Failed to instantiate API: {}", clazz.getName(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(API[]::new);
    }
}