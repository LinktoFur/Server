package net.linktofur.captcha;

import lombok.AllArgsConstructor;

/**
 * @author LangYa466
 * @date 2026/3/26
 */
@AllArgsConstructor
public class Captcha {
    public String text;
    public long expireAt;
}