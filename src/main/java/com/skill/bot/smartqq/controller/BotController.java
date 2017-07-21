package com.skill.bot.smartqq.controller;


import com.alibaba.fastjson.JSONArray;
import com.skill.bot.smartqq.client.SmartQQClient;

import com.skill.util.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * bot的控制器
 * 用来加各种功能
 *
 * @author dullwolf
 */

public class BotController {

    private static final int MESSAGE_TYPE_PRIVATE = 0;
    private static final int MESSAGE_TYPE_GROUP = 1;
    private static final int MESSAGE_TYPE_DISCUSS = 2;


    private static SmartQQClient client;

    private static Logger LOGGER = LoggerFactory.getLogger(BotController.class);

    public BotController(SmartQQClient client){
        BotController.client = client;
    }



    private void sendMessage(String msg,Long id,int type){
        switch (type){
            case MESSAGE_TYPE_PRIVATE:
                System.out.println("发送私聊信息");
                client.sendMessageToFriend(id,msg);
                break;
            case MESSAGE_TYPE_GROUP:
                System.out.println("发送群信息");
                client.sendMessageToGroup(id,msg);
                break;
            case MESSAGE_TYPE_DISCUSS:
                System.out.println("发送讨论组信息");
                client.sendMessageToDiscuss(id,msg);
                break;
            default:
                LOGGER.error(String.format("发送类型错误(%s)",type));
                break;
        }
    }

    public void roll(String name,Long id,int type){
        sendMessage(String.format("@%s\nroll点:%d",name,(int)(Math.random() * 100 + 1)),id,type);
    }

    public void rolls(String name,Long id,int type,String [] params){
        StringBuilder sb = new StringBuilder("@"+name+"\n");
        for(int i=1;i<=params.length-1;i++){
            sb.append("第").append(i).append("个参数为：").append(params[i]).append("\n");
        }
        sendMessage(sb.toString(),id,type);
    }

    public void home(String name, final Long id, final int type, final String [] params){
        try {
            final String[] str = {null};
            if(params.length > 1 ){
                if("新番资源".equals(params[1])){
                    str[0] = "@"+name+"\n"+"http://abc.dullwolf.xyz/XinFan/";
                }else{
                    final int ll = 0,pc = 0,tth = 0;
                    final String ranks = "↑0",accs = "+0",pps = "+0";
                    final String[] strs = {null};

                    //接下来调用osu接口！
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            strs[0] = sendOsuApi(params[1], str[0], ll, pc, tth, ranks, accs, pps,0);
                            if(null != strs[0]){
                                sendMessage(strs[0],id,type);
                            }
                        }
                    }).start();
                }
            }
            if(null != str[0]){
                sendMessage(str[0],id,type);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void mania(String name, final Long id, final int type, final String [] params){
        try {
            final String[] str = {null};
            if(params.length > 1 ){
                final int ll = 0,pc = 0,tth = 0;
                final String ranks = "↑0",accs = "+0",pps = "+0";
                final String[] strs = {null};

                //接下来调用osu接口！
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        strs[0] = sendOsuApi(params[1], str[0], ll, pc, tth, ranks, accs, pps,3);
                        if(null != strs[0]){
                            sendMessage(strs[0],id,type);
                        }
                    }
                }).start();
            }
            if(null != str[0]){
                sendMessage(str[0],id,type);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void taiko(String name, final Long id, final int type, final String [] params){
        try {
            final String[] str = {null};
            if(params.length > 1 ){
                final int ll = 0,pc = 0,tth = 0;
                final String ranks = "↑0",accs = "+0",pps = "+0";
                final String[] strs = {null};

                //接下来调用osu接口！
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        strs[0] = sendOsuApi(params[1], str[0], ll, pc, tth, ranks, accs, pps,1);
                        if(null != strs[0]){
                            sendMessage(strs[0],id,type);
                        }
                    }
                }).start();
            }
            if(null != str[0]){
                sendMessage(str[0],id,type);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void ctb(String name, final Long id, final int type, final String [] params){
        try {
            final String[] str = {null};
            if(params.length > 1 ){
                final int ll = 0,pc = 0,tth = 0;
                final String ranks = "↑0",accs = "+0",pps = "+0";
                final String[] strs = {null};

                //接下来调用osu接口！
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        strs[0] = sendOsuApi(params[1], str[0], ll, pc, tth, ranks, accs, pps,2);
                        if(null != strs[0]){
                            sendMessage(strs[0],id,type);
                        }
                    }
                }).start();
            }
            if(null != str[0]){
                sendMessage(str[0],id,type);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private String sendOsuApi(String param, String str, int ll, int pc, int tth, String ranks, String accs, String pps,int mode) {
        double pp;
        double acc;
        int rank;
        String s = null;
        try {
            s = HttpRequest.doGet("https://osu.ppy.sh/api/get_user?k=cda7da667c3c0d6b36e1ef1e7c1941e28e984310&m="+mode+"&u="+ param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if( null != s){
            List<Map<String,Object>> info = (List<Map<String, Object>>) JSONArray.parse(s);
            if(!"[]".equals(info.toString())){

//                MyOSU myOSU = myOSUService.selInfoByUserId(Integer.parseInt(info.get(0).get("user_id").toString()),mode);
//                if(null == myOSU){
//                    //用户是第一次使用，数据库不存在，于是执行插入
//                    Map<String,Object> m = new HashMap<String, Object>();
//                    m.put("userId",info.get(0).get("user_id"));
//                    m.put("acc",acc(info.get(0).get("accuracy").toString()));
//                    m.put("level",(int) Double.parseDouble(info.get(0).get("level").toString()));
//                    m.put("pc",info.get(0).get("playcount"));
//                    m.put("pp",acc(info.get(0).get("pp_raw").toString()));
//                    m.put("rank",info.get(0).get("pp_rank"));
//                    m.put("tth",add(info.get(0).get("count300").toString(),info.get(0).get("count100").toString(),info.get(0).get("count50").toString()));
//                    m.put("username",info.get(0).get("username"));
//                    m.put("mode",mode);
//                    myOSUService.insertData(m);
//
//                }else{
//                    ll = (int) Double.parseDouble(info.get(0).get("level").toString()) - myOSU.getLevel();
//                    pp = Double.parseDouble(acc(info.get(0).get("pp_raw").toString())) - myOSU.getPp();
//                    if(pp < 0){
//                        pps = "-"+acc(""+(-1)*pp);
//                    }else{
//                        pps = "+"+acc(""+pp);
//                    }
//                    acc = Double.parseDouble(acc(info.get(0).get("accuracy").toString())) - myOSU.getAcc();
//                    if(acc < 0){
//                        accs = "-"+acc(""+(-1)*acc);
//                    }else{
//                        accs = "+"+acc(""+acc);
//                    }
//                    pc = Integer.parseInt(info.get(0).get("playcount").toString()) - myOSU.getPc();
//                    tth = add(info.get(0).get("count300").toString(), info.get(0).get("count100").toString(), info.get(0).get("count50").toString()) - myOSU.getTth();
//                    rank = Integer.parseInt(info.get(0).get("pp_rank").toString()) - myOSU.getRank();
//                    if(rank <= 0){
//                        ranks = "↑"+(-1)*rank;
//                    }else{
//                        ranks = "↓"+rank;
//                    }
//                }


                String modes = "";
                if(0 == mode){
                    modes = "std";
                }else if(1 == mode){
                    modes = "Taiko";
                }else if(2 == mode){
                    modes = "CtB";
                }else if(3 == mode){
                    modes = "mania";
                }

                str =   "玩家主页：https://osu.ppy.sh/u/"+info.get(0).get("user_id")+"\n"+
                        "用户名称: "+info.get(0).get("username")+"\n" +
                        "mode: "+modes+"\n" +
                        "level: "+(int) Double.parseDouble(info.get(0).get("level").toString())+"（+"+ll+"）"+"\n" +
                        "pp: "+acc(info.get(0).get("pp_raw").toString())+"（"+pps+"）"+"\n" +
                        "accuracy: "+acc(info.get(0).get("accuracy").toString())+"%"+"（"+accs+"）"+"\n" +
                        "playCount: "+info.get(0).get("playcount").toString()+"（+"+pc+"）"+"\n" +
                        "tth: "+add(info.get(0).get("count300").toString(),info.get(0).get("count100").toString(),info.get(0).get("count50").toString())+"（+"+tth+"）"+"\n" +
                        "全球排名: "+info.get(0).get("pp_rank").toString()+"（"+ranks+"）"+"\n"+
                        "[注] 括号里面的数字表示和昨天的成绩的对比，每天凌晨3点数据库重新更新";
            }
        }

        return str;
    }

    private static String acc(String s){
        return String.format("%.2f",Double.parseDouble(s));
    }

    private static Integer add(String t300,String t100,String t50){
        return Integer.parseInt(t300)+Integer.parseInt(t100)+Integer.parseInt(t50);
    }
}
