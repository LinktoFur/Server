import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2026/3/18
 */
public class Data2StringTest {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("测试问题1","回答123");
        System.out.println(map);
        List<String> list = new ArrayList<>();
        list.add("问题12312");
        System.out.println(list);
    }
}
