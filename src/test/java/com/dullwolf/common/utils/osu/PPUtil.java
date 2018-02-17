package com.dullwolf.common.utils.osu;

import com.dullwolf.common.utils.CmdUtil;
import com.dullwolf.common.utils.StringUtil;
import dp.oppai.StatsPP;

import static com.dullwolf.common.utils.CmdUtil.sendMessage;


public class PPUtil {

    public static void getPP(long id, int type, String params) {
        String param = CmdUtil.getTrimParams(CmdUtil.getParams(params,2));
        if(!"".equals(param)){
            String allMod = "EZNFHTHRDTNCHDFLSOSDPF";
            String mod = "";
            long bid = 0;
            double acc = -1;
            boolean flag = true;
            String[] split = param.split(" ");
            if(split.length == 3){
                try {
                    bid = Long.parseLong(split[0].trim());
                    acc = Double.parseDouble(acc(split[2].trim()));
                    if(acc > 100 || acc < 80){
                        flag = false;
                        sendMessage("acc范围值在80-100之间", id, type);
                    }
                    if(allMod.contains(split[1].toUpperCase().substring(0,2))){
                        mod = split[1];
                    }else{
                        sendMessage("大佬！你输入的mod格式不对，系统已默认设置为None", id, type);
                    }
                } catch (NumberFormatException e) {
                    flag = false;
                    sendMessage("请检查歌谱id或者acc是否为数字类型的参数！", id, type);
                } catch (StringIndexOutOfBoundsException e){
                    sendMessage("大佬！你输入的mod格式不对，系统已默认设置为None", id, type);
                }
            }else if(split.length == 2){
                try {
                    bid = Long.parseLong(split[0].trim());
                    acc = Double.parseDouble(acc(split[1].trim()));
                    if(acc > 100 || acc < 80){
                        flag = false;
                        sendMessage("acc范围值在80-100之间", id, type);
                    }
                } catch (NumberFormatException e) {
                    //在只有两个参数情况下，第二个参数不是acc 那就是mod
                    try {
                        if(allMod.contains(split[1].toUpperCase().substring(0,2))){
                            mod = split[1];
                        }else{
                            sendMessage("大佬！你输入的mod格式不对，系统已默认设置为None", id, type);
                        }
                    } catch (StringIndexOutOfBoundsException e1) {
                        sendMessage("大佬！你输入的mod格式不对，系统已默认设置为None", id, type);
                    }
                }
            }else if(split.length == 1){
                try {
                    bid = Long.parseLong(split[0].trim());
                } catch (NumberFormatException e) {
                    flag = false;
                    sendMessage("请检查歌谱id是否为数字类型的参数！", id, type);
                }
            }else{
                sendMessage("没有第四个参数了吧？！", id, type);
            }
            if(flag){
                StatsPP.stats(id, type, bid, mod, acc);
            }
        }else{
            sendMessage("必须要输入歌谱ID的参数！", id, type);
        }
    }

    private static String acc(String s) {
        return String.format("%.2f", Double.parseDouble(s));
    }
}
