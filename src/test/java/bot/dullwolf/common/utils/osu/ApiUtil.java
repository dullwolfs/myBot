package bot.dullwolf.common.utils.osu;


import bot.dullwolf.common.utils.HttpRequest;

public class ApiUtil {

    private static String loopSend(String url) {
        String str = null;
        String data;
        int count = 20;
        while (count > 0) {
            try {
                data = HttpRequest.doGet(url);
                if (null != data) {
                    str = data;
                    //获取成功 结束循环
                    break;
                } else {
                    count--;
                }
            } catch (Exception e) {
                count--;
            }
        }
        return str;
    }

    public static String getOsuApiStr(String username,int mode) {
        username = username.replaceAll(" ", "%20");
        String url = "https://osu.ppy.sh/api/get_user?k=cda7da667c3c0d6b36e1ef1e7c1941e28e984310&m=" + mode + "&u=" + username;
        return loopSend(url);
    }

    public static String getRecentApi(String username,int mode) {
        username = username.replaceAll(" ", "%20");
        String url = "https://osu.ppy.sh/api/get_user_recent?k=cda7da667c3c0d6b36e1ef1e7c1941e28e984310&limit=1&m="+mode+"&u="+username;
        return loopSend(url);
    }

    public static String getScoreApi(String username,int mode,String bid) {
        username = username.replaceAll(" ", "%20");
        String url = "https://osu.ppy.sh/api/get_scores?k=cda7da667c3c0d6b36e1ef1e7c1941e28e984310&b="+bid+"&m="+mode+"&u="+username;
        return loopSend(url);
    }


}
