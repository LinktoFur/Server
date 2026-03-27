package net.linktofur.captcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Slf4j
public class CaptchaManager {
    public static final CaptchaManager INSTANCE = new CaptchaManager();
    private static final long EXPIRE_MS = 5 * 60 * 1000; // 5分钟过期
    private final Map<String, Captcha> captchas = new ConcurrentHashMap<>();
    private final DefaultKaptcha kaptcha;

    public CaptchaManager() {
        kaptcha = new DefaultKaptcha();
        var props = new Properties();
        props.setProperty("kaptcha.image.width", "160");
        props.setProperty("kaptcha.image.height", "50");
        props.setProperty("kaptcha.textproducer.char.length", "4");
        props.setProperty("kaptcha.textproducer.char.string", "0123456789");
        props.setProperty("kaptcha.textproducer.font.size", "36");
        props.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        props.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");
        props.setProperty("kaptcha.background.clear.from", "245,245,245");
        props.setProperty("kaptcha.background.clear.to", "220,220,220");
        kaptcha.setConfig(new Config(props));
    }

    // [captchaId, base64Image]
    public String[] generate() {
        try {
            var text = kaptcha.createText();
            var image = kaptcha.createImage(text);

            var baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            var captchaId = UUID.randomUUID().toString();
            captchas.put(captchaId, new Captcha(text, System.currentTimeMillis() + EXPIRE_MS));

            return new String[]{captchaId, "data:image/png;base64," + base64};
        } catch (Exception e) {
            log.error("Failed to generate captcha", e);
            return null;
        }
    }

    public boolean validate(String captchaId, String userAnswer) {
        if (captchaId == null || userAnswer == null) return false;

        var entry = captchas.remove(captchaId);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expireAt) return false;

        return entry.text.equalsIgnoreCase(userAnswer.trim());
    }
}