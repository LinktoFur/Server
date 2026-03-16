package net.linktofur.api;

import net.linktofur.api.impl.TestAPI;
import net.linktofur.api.impl.user.LogOutAPI;
import net.linktofur.api.impl.user.LoginAPI;
import net.linktofur.api.impl.user.RegisterAPI;
import net.linktofur.api.impl.user.VerifyAPI;
import lombok.extern.slf4j.Slf4j;
import net.linktofur.api.impl.*;
import net.linktofur.api.impl.user.*;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
@Slf4j
public class APIManager {
    public static final APIManager INSTANCE = new APIManager();
    public API[] apis;

    public APIManager() {
        apis = new API[]{
                new TestAPI(),
                new LoginAPI(),
                new RegisterAPI(),
                new LogOutAPI(),
                new VerifyAPI()
        };
        for (API api : apis) {
            log.info("API {} has been initialized", api);
        }
    }
}