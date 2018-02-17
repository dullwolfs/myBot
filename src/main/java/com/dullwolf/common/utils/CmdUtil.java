package com.dullwolf.common.utils;


import com.dullwolf.common.Constant;
import com.dullwolf.common.utils.io.RedisUtil;
import com.dullwolf.common.utils.osu.ApiUtil;
import com.dullwolf.common.utils.osu.CalcPPUtil;
import com.dullwolf.common.utils.osu.ModUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdUtil {

    private static Logger logger = LoggerFactory.getLogger(CmdUtil.class);

    private static final int MESSAGE_TYPE_PRIVATE = 0;
    private static final int MESSAGE_TYPE_GROUP = 1;
    private static final int MESSAGE_TYPE_DISCUSS = 2;

    public static String reset(long userId, long id, int type, String osuName) {
        RedisUtil redisUtil = RedisUtil.getRu();
        if("".equals(osuName)){
            //判断参数为空的情况，就找redis有没有记录
            String username = redisUtil.hget(Constant.USERTAG + userId, "username");
            if(StringUtil.isNullOrEmpty(username)){
                CmdUtil.sendMessage("请输入参数!", id, type);
            }
        }else{
            //不管用户第几次输入osuName，重新set进redis
            Map<String,String> map = new HashMap<>();
            map.put("username",osuName);
            redisUtil.hmset(Constant.USERTAG + userId,map);

        }
        return redisUtil.hget(Constant.USERTAG + userId, "username");
    }


    public static void sendMessage(String msg, long id, int type) {
        switch (type) {
            case MESSAGE_TYPE_PRIVATE:
                CqUtil.sendMsg(id, msg, "private");
                break;
            case MESSAGE_TYPE_GROUP:
                CqUtil.sendMsg(id, msg, "group");
                break;
            case MESSAGE_TYPE_DISCUSS:
                CqUtil.sendMsg(id, msg, "discuss");
                break;
            default:
                logger.error(String.format("发送类型错误(%s)", type));
                break;
        }
    }

    public static void help(long id, int type) {
        String str = "欢迎使用蠢狼机器人，输入命令有：\n" +
                "!home 玩家名字\n" +
                "注:这个命令是查询OSU成绩的功能，home默认是std模式\n" +
                "此外其他模式查询输入命令分别为：!ctb、!taiko、!mania\n" +
                "\n" +
                "!recent 玩家名字\n" +
                "注:这个命令是查询玩家最近玩的一首歌的成绩，默认是std模式\n" +
                "此外其他模式查询输入命令分别为：!cr、!tr、!mr\n" +
                "(有关OSU的命令，第一次输入带玩家名字参数，第二次可以不用)\n" +
                "\n" +
                "!roll\n" +
                "注:这个命令是返回1-100的随机数";
        sendMessage(str, id, type);
    }

    private static String acc(String s) {
        return String.format("%.2f", Double.parseDouble(s));
    }

    public static String getParams(String params ,int num){
        String trim = params.substring(1, params.length()).trim();
        String param = trim.substring(num, trim.length()).trim();
        param = param.replaceAll("&#91;", "[");
        param = param.replaceAll("&#93;", "]");
        return param;
    }

    public static String getLowerParams(String msg) {
        Pattern p = Pattern.compile("\\s+");
        Matcher m = p.matcher(msg);
        return m.replaceAll(" ").toLowerCase().trim();
    }

    public static String getTrimParams(String msg) {
        Pattern p = Pattern.compile("\\s+");
        Matcher m = p.matcher(msg);
        return m.replaceAll(" ").trim();
    }

    public static void roll(long id, int type) {
        sendMessage(String.format("roll点:%d", (int) (Math.random() * 100 + 1)), id, type);
    }

    public static void pp(long id, int type, String params) {
        sendMessage("该算法目前还在完善中...", id, type);
        //getPP(id, type, params);
    }

    public static void recent(long userId,long id, int type, String params,String name) {
        int mode = 0;
        String osuName = "";
        String modeName = "std";
        if(name.contains("recent")){
            osuName = reset(userId, id, type, getParams(params,6));
        } else if (name.contains("tr")) {
            mode = 1;
            modeName = "TaiKo";
            osuName = reset(userId, id, type, getParams(params,2));
        } else if (name.contains("cr")) {
            mode = 2;
            modeName = "CtB";
            osuName = reset(userId, id, type, getParams(params,2));
        } else if (name.contains("mr")) {
            mode = 3;
            modeName = "mania";
            osuName = reset(userId, id, type, getParams(params,2));
        }
        if(!StringUtil.isNullOrEmpty(osuName)) {
            //重新reset之后，进入recent的方法
            StringBuilder sb = new StringBuilder();
            String bid;
            List<Map> info = JsonUtils.getArrayByJson(ApiUtil.getRecentApi(osuName,mode), Map.class);
            if(ArrayUtil.isNotEmpty(info)){
                Map data = info.get(0);
                bid = data.get("beatmap_id").toString();
                if(!"".equals(bid)){
                    String score = data.get("score").toString();
                    String maxCombo = data.get("maxcombo").toString();
                    String date = data.get("date").toString();
                    String rank = data.get("rank").toString();
                    //反转mod
                    int mods = Integer.parseInt(data.get("enabled_mods").toString());
                    String modStr = ModUtil.convertMOD(mods).keySet().toString().replaceAll("\\[", "").replaceAll("]","");
                    //计算acc
                    int count50 = Integer.parseInt(data.get("count50").toString());
                    int count100 = Integer.parseInt(data.get("count100").toString());
                    int count300 = Integer.parseInt(data.get("count300").toString());
                    int countmiss = Integer.parseInt(data.get("countmiss").toString());
                    int countkatu = Integer.parseInt(data.get("countkatu").toString());
                    int countgeki = Integer.parseInt(data.get("countgeki").toString());
                    double acc = 0;
                    switch (mode){
                        case 0:
                            acc = (count300 * 300.0 + count100 * 100.0 + count50 * 50.0) / ((count300 + count100 + count50 + countmiss) * 300);
                            break;
                        case 1:
                            acc = (count300 + count100 * 0.5) / (count300 + count100 + countmiss);
                            break;
                        case 2:
                            int base = count300 + count100 + count50;
                            acc = (double) base / (base + (countmiss + countkatu));
                            break;
                        case 3:
                            acc = (double)(count50 * 50 + count100 * 100 + countkatu * 200 + (count300+countgeki) * 300) / ((count300 + count100 + count50 + countmiss + countkatu + countgeki)*300);
                            break;
                        default:
                            break;
                    }
                    double accS = Double.parseDouble(acc((acc * 100) + ""));
                    sb.append("歌谱：").append("https://osu.ppy.sh/b/").append(bid).append("\n");
                    sb.append("玩家：").append(osuName).append("\n");
                    sb.append("模式：").append(modeName).append("\n");
                    sb.append("Score：").append(StringUtil.fen(score)).append("\n");
                    sb.append("Rank：").append(rank).append("\n");
                    sb.append("Mod：").append(modStr).append("\n");
                    sb.append("Acc：").append(accS).append("\n");
                    sb.append("maxCombo：").append(maxCombo).append("\n");
                    sb.append("Date：").append(date).append("\n");
                    List<Map> scoreList = JsonUtils.getArrayByJson(ApiUtil.getScoreApi(osuName,mode,bid), Map.class);
                    if(ArrayUtil.isNotEmpty(scoreList)){
                        Map ppData = scoreList.get(scoreList.size()-1);
                        double pp;
                        if(!StringUtil.isNullOrEmpty(ppData.get("pp"))){
                            pp = Double.parseDouble(ppData.get("pp").toString());
                        }else{
                            Map<String,Object> scoreMap = new HashMap<>();
                            scoreMap.put("mods",mods);
                            scoreMap.put("mode",mode);
                            scoreMap.put("n300",count300);
                            scoreMap.put("n100",count100);
                            scoreMap.put("n50",count50);
                            scoreMap.put("nmiss",countmiss);
                            scoreMap.put("combo",maxCombo);
                            pp = Double.parseDouble(acc(CalcPPUtil.calcPP(bid, scoreMap) + ""));
                        }
                        sb.append(ppTip(rank,pp));
                    }else{
                        sb.append("\n要打完一首才有PP哦！");
                    }
                    CmdUtil.sendMessage(sb.toString(),id,type);
                }
            }else{
                CmdUtil.sendMessage("玩家 "+osuName+" 最近没有数据记录，请先玩一把OSU（如果还是查询失败，看看是不是名字输错）",id,type);
            }
        }
    }

    private static String ppTip(String rank,double pp) {
        StringBuilder sb = new StringBuilder();
        if(-1 == pp){
            sb.append("\nPP计算出错...");
        }else if(0 == Math.round(pp)){
            sb.append("\n您玩得太菜了，没PP...");
        }else{
            if("F".equals(rank)){
                sb.append("\nPP：").append(Math.round(pp)).append("，可惜未打完的歌曲，PP不上传...");
            }else{
                sb.append("\n获得PP：").append(Math.round(pp));
            }
        }
        return sb.toString();
    }

}
