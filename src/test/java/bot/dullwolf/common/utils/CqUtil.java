package bot.dullwolf.common.utils;

import bot.dullwolf.pojo.CqResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

//将CQ的HTTP API封装为接口，并托管到Spring
@Component
public class CqUtil {

    public static CqResponse sendMsg(long id,String msg,String type) {
        String baseURL = "http://localhost:5700";
        Map<String,Object> info = new HashMap<>();
        String URL;
        switch (type) {
            case "group":
                URL = baseURL + "/send_group_msg";
                info.put("group_id",id);
                info.put("message",msg);
                break;
            case "discuss":
                URL = baseURL + "/send_discuss_msg";
                info.put("discuss_id",id);
                info.put("message",msg);
                break;
            case "private":
                URL = baseURL + "/send_private_msg";
                info.put("user_id",id);
                info.put("message",msg);
                break;
            default:
                return null;
        }
        String result = HttpRequest.doPost(JsonUtils.getJsonString(info), URL);
        return JsonUtils.getObjectByJson(result, CqResponse.class);
    }

}
