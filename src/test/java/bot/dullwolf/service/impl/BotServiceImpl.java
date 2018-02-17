package bot.dullwolf.service.impl;

import bot.dullwolf.common.utils.*;
import bot.dullwolf.common.utils.osu.ApiUtil;
import bot.dullwolf.dao.MyOSUDao;
import bot.dullwolf.model.MyOSU;
import bot.dullwolf.service.BotService;
import bot.dullwolf.pojo.CqMsg;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dullwolf
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BotServiceImpl implements BotService {

    private static final int MESSAGE_TYPE_PRIVATE = 0;
    private static final int MESSAGE_TYPE_GROUP = 1;
    private static final int MESSAGE_TYPE_DISCUSS = 2;

    @Autowired
    private MyOSUDao myOSUDao;

    /**
     * 根据用户名Id查找详细信息
     * @param userId 用户ID
     * @param mode 模式
     */
    @Override
    @Transactional(readOnly = true)
    public MyOSU selInfoByUserId(Integer userId, Integer mode) {
        Integer id = myOSUDao.selectIdByUserId(userId,mode);
        if(null != id){
            return myOSUDao.selectByPrimaryKey(id);
        }
        return null;
    }

    /**
     * 插入数据
     */
    @Override
    public void insertData(Map<String, Object> m) {
        MyOSU myOSU = new MyOSU();
        myOSU.setUserId(Integer.parseInt(m.get("userId").toString()));
        myOSU.setAcc(Double.parseDouble(m.get("acc").toString()));
        myOSU.setLevel(Integer.parseInt(m.get("level").toString()));
        myOSU.setPc(Integer.parseInt(m.get("pc").toString()));
        myOSU.setPp(Double.parseDouble(m.get("pp").toString()));
        myOSU.setRank(Integer.parseInt(m.get("rank").toString()));
        myOSU.setTth(Integer.parseInt(m.get("tth").toString()));
        myOSU.setUsername(m.get("username").toString());
        myOSU.setMode(Integer.parseInt(m.get("mode").toString()));
        myOSU.setCreateTime(new Date());
        myOSU.setUpdateTime(new Date());
        myOSU.setRankscore(m.get("ranked").toString());
        myOSUDao.insertSelective(myOSU);

    }

    /**
     * 更新数据
     */
    @Override
    public void updateData() {
        List<Map<String, Object>> osuInfo = myOSUDao.selAllOSUInfo();
        for(Map<String,Object> map : osuInfo){
            Integer id = Integer.parseInt(map.get("id").toString());
            String userId = map.get("userId").toString();
            Integer mode = Integer.parseInt(map.get("mode").toString());

            int count = 20;
            String s;
            while (count > 0){
                try {
                    s = HttpRequest.doGet("https://osu.ppy.sh/api/get_user?k=cda7da667c3c0d6b36e1ef1e7c1941e28e984310&m=" + mode + "&u=" + userId);
                    if(null != s){
                        List<Map> info = JsonUtils.getArrayByJson(s, Map.class);
                        if(ArrayUtils.isNotEmpty(info.toArray())){
                            MyOSU myOSU = new MyOSU();
                            myOSU.setId(id);
                            myOSU.setAcc(Double.parseDouble(acc(info.get(0).get("accuracy").toString())));
                            myOSU.setLevel((int) Double.parseDouble(info.get(0).get("level").toString()));
                            myOSU.setPc(Integer.parseInt(info.get(0).get("playcount").toString()));
                            myOSU.setPp(Double.parseDouble(acc(info.get(0).get("pp_raw").toString())));
                            myOSU.setRank(Integer.parseInt(info.get(0).get("pp_rank").toString()));
                            myOSU.setTth(add(info.get(0).get("count300").toString(), info.get(0).get("count100").toString(), info.get(0).get("count50").toString()));
                            myOSU.setUserId(Integer.parseInt(userId));
                            myOSU.setUsername(info.get(0).get("username").toString());
                            myOSU.setUpdateTime(new Date());
                            myOSU.setRankscore(info.get(0).get("ranked_score").toString());
                            myOSUDao.updateByPrimaryKeySelective(myOSU);
                        }
                        break;
                    }else{
                        count --;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void sendCqMsg(String type,CqMsg cqMsg) {
        if(StringUtil.notEmpty(type)){
            String msgData;
            switch (type) {
                case "group":
                    msgData = cqMsg.getMessage().trim();
                    if (msgData.startsWith("!") || msgData.startsWith("！")) {
                        String params = CmdUtil.getTrimParams(msgData);
                        String cmd = CmdUtil.getLowerParams(msgData.substring(1,msgData.length()));
                        handle2(cmd, MESSAGE_TYPE_GROUP, cqMsg,params);
                    }
                    break;
                case "discuss":
                    msgData = cqMsg.getMessage().trim();
                    if (msgData.startsWith("!") || msgData.startsWith("！")) {
                        String params = CmdUtil.getTrimParams(msgData);
                        String cmd = CmdUtil.getLowerParams(msgData.substring(1,msgData.length()));
                        handle3(cmd, MESSAGE_TYPE_DISCUSS, cqMsg,params);
                    }
                    break;
                case "private":
                    msgData = cqMsg.getMessage().trim();
                    if (msgData.startsWith("!") || msgData.startsWith("！")) {
                        String params = CmdUtil.getTrimParams(msgData);
                        String cmd = CmdUtil.getLowerParams(msgData.substring(1,msgData.length()));
                        handle(cmd, MESSAGE_TYPE_PRIVATE, cqMsg,params);
                    }
                    break;
                default:
                    break;
            }
        }

    }


    private void handle(String cmd, int type, CqMsg msg,String params){
        switchs(cmd,msg.getUserId(),type,params);
    }

    private void handle2(String cmd, int type, CqMsg msg,String params){
        switchs(cmd,msg.getUserId(),msg.getGroupId(),type,params);

    }

    private void handle3(String cmd, int type, CqMsg msg,String params){
        switchs(cmd,msg.getUserId(),msg.getDiscussId(),type,params);

    }

    private void switchs(String cmd,long id,int type,String params){
        switchs(cmd,id,id,type,params);

    }
    private void switchs(String cmd,long userId,long id,int type,String params){
        if(cmd.startsWith("roll")){
            CmdUtil.roll(id,type);
        }else if(cmd.startsWith("help")){
            CmdUtil.help(id,type);
        }else if(cmd.startsWith("pp")){
            CmdUtil.pp(id,type,params);
        }else if(cmd.startsWith("home") || cmd.startsWith("mania") ||cmd.startsWith("taiko") ||cmd.startsWith("ctb")){
            testHome(userId,id,type,params,cmd);
        }else if(cmd.startsWith("recent") || cmd.startsWith("mr") ||cmd.startsWith("tr") ||cmd.startsWith("cr")){
            CmdUtil.recent(userId,id,type,params,cmd);
        }

    }

    private void testHome(long userId,long id, int type, String params, String name) {
        int mode = 0;
        String osuName = "";
        if(name.contains("home")){
            osuName = CmdUtil.reset(userId, id, type, CmdUtil.getParams(params,4));
        } else if (name.contains("taiko")) {
            mode = 1;
            osuName = CmdUtil.reset(userId, id, type, CmdUtil.getParams(params,5));
        } else if (name.contains("ctb")) {
            mode = 2;
            osuName = CmdUtil.reset(userId, id, type, CmdUtil.getParams(params,3));
        } else if (name.contains("mania")) {
            mode = 3;
            osuName = CmdUtil.reset(userId, id, type, CmdUtil.getParams(params,5));
        }
        if(!StringUtil.isNullOrEmpty(osuName)){
            try {
                int ll = 0, pc = 0, tth = 0;
                String ranks = "↑0", accs = "+0", pps = "+0", rankeds = "+0";
                String msg = sendOsuApi(osuName, ll, pc, tth, ranks, accs, pps, rankeds, mode);
                if(null != msg){
                    CmdUtil.sendMessage(msg,id,type);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    private String sendOsuApi(String username, int ll, int pc, int tth, String ranks, String accs, String pps, String rankeds, int mode) {
        String str;
        double pp;
        double acc;
        int rank;
        long ranked;
        List<Map> info = JsonUtils.getArrayByJson(ApiUtil.getOsuApiStr(username,mode), Map.class);
        if (ArrayUtil.isNotEmpty(info)) {
            if (null == info.get(0).get("playcount")) {
                return "玩家主页：https://osu.ppy.sh/u/" + info.get(0).get("user_id") + "\n" +
                        "该玩家不是机器人就是僵尸！求大佬别再查这家伙的成绩了";
            }
            MyOSU myOSU = selInfoByUserId(Integer.parseInt(info.get(0).get("user_id").toString()), mode);
            if (null == myOSU) {
                //用户是第一次使用，数据库不存在，于是执行插入
                Map<String, Object> m = new HashMap<>();
                m.put("userId", info.get(0).get("user_id"));
                m.put("acc", acc(info.get(0).get("accuracy").toString()));
                m.put("level", (int) Double.parseDouble(info.get(0).get("level").toString()));
                m.put("pc", info.get(0).get("playcount"));
                m.put("pp", acc(info.get(0).get("pp_raw").toString()));
                m.put("rank", info.get(0).get("pp_rank"));
                m.put("tth", add(info.get(0).get("count300").toString(), info.get(0).get("count100").toString(), info.get(0).get("count50").toString()));
                m.put("username", info.get(0).get("username"));
                m.put("mode", mode);
                m.put("ranked", info.get(0).get("ranked_score"));
                insertData(m);

            } else {
                ll = (int) Double.parseDouble(info.get(0).get("level").toString()) - myOSU.getLevel();
                pp = Double.parseDouble(acc(info.get(0).get("pp_raw").toString())) - myOSU.getPp();
                if (pp < 0) {
                    pps = "-" + acc("" + (-1) * pp);
                } else {
                    pps = "+" + acc("" + pp);
                }
                acc = Double.parseDouble(acc(info.get(0).get("accuracy").toString())) - myOSU.getAcc();
                if (acc < 0) {
                    accs = "-" + acc("" + (-1) * acc);
                } else {
                    accs = "+" + acc("" + acc);
                }
                pc = Integer.parseInt(info.get(0).get("playcount").toString()) - myOSU.getPc();
                tth = add(info.get(0).get("count300").toString(), info.get(0).get("count100").toString(), info.get(0).get("count50").toString()) - myOSU.getTth();
                rank = Integer.parseInt(info.get(0).get("pp_rank").toString()) - myOSU.getRank();
                if (rank <= 0) {
                    ranks = "↑" + (-1) * rank;
                } else {
                    ranks = "↓" + rank;
                }

                ranked = Long.parseLong(info.get(0).get("ranked_score").toString()) - Long.parseLong(myOSU.getRankscore());
                if (ranked < 0) {
                    rankeds = "-" + StringUtil.fen(((-1) * ranked)+"");
                } else {
                    rankeds = "+" + StringUtil.fen(ranked+"");
                }
            }
            String modes = "";
            if (0 == mode) {
                modes = "std";
            } else if (1 == mode) {
                modes = "Taiko";
            } else if (2 == mode) {
                modes = "CtB";
            } else if (3 == mode) {
                modes = "mania";
            }

            String fen =  100 - (Double.parseDouble(info.get(0).get("level").toString()) - (int) Double.parseDouble(info.get(0).get("level").toString()))*100 +"";

            str = "玩家主页：https://osu.ppy.sh/u/" + info.get(0).get("user_id") + "\n" +
                    "用户名称: " + info.get(0).get("username") + "\n" +
                    "mode: " + modes + "\n" +
                    "level: " + (int) Double.parseDouble(info.get(0).get("level").toString()) + "（+" + ll +"）距离升级还剩有"+acc(fen)+"%" + "\n" +
                    "pp: " + acc(info.get(0).get("pp_raw").toString()) + "（" + pps + "）" + "\n" +
                    "accuracy: " + acc(info.get(0).get("accuracy").toString()) + "%" + "（" + accs + "）" + "\n" +
                    "playCount: " + info.get(0).get("playcount").toString() + "（+" + pc + "）" + "\n" +
                    "tth: " + add(info.get(0).get("count300").toString(), info.get(0).get("count100").toString(), info.get(0).get("count50").toString()) + "（+" + tth + "）" + "\n" +
                    "ranked Score: " + StringUtil.fen(info.get(0).get("ranked_score").toString()) + "（" + rankeds + "）" + "\n" +
                    "全球排名: " + info.get(0).get("pp_rank").toString() + "（" + ranks + "）" + "\n" +
                    "[注] 括号里面的数字表示和昨天的成绩的对比，每天凌晨3点数据库重新更新";

        } else {
            str = "找不到[" + username + "]，该玩家的信息，你确定不是乱搞的？！";
        }

        return str;
    }

    private static String acc(String s) {
        return String.format("%.2f", Double.parseDouble(s));
    }

    private static Integer add(String t300, String t100, String t50) {
        return Integer.parseInt(t300) + Integer.parseInt(t100) + Integer.parseInt(t50);
    }

}
