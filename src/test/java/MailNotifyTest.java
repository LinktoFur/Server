import net.linktofur.util.NotifyUtil;

/**
 * @author LangYa466
 * @date 2026/2/27
 */
public class MailNotifyTest {
    public static void main(String[] args) throws Exception {
        try {
            NotifyUtil.MAIL.send("LinkToFur test","my test message", "3054086606@qq.com");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
