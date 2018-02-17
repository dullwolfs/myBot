package com.dullwolf.common.utils.osu;

import com.dullwolf.common.utils.StringUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by dullwolf on 2018/1/20.
 */
public class CalcPPUtil {

    public static double calcPP(String bid, Map<String,Object> score) {

        int mods = Integer.parseInt(score.get("mods").toString());
        int n300 = Integer.parseInt(score.get("n300").toString());
        int n100 = Integer.parseInt(score.get("n100").toString());
        int n50 = Integer.parseInt(score.get("n50").toString());
        int nmiss = Integer.parseInt(score.get("nmiss").toString());
        int combo = Integer.parseInt(score.get("combo").toString());
        int mode = Integer.parseInt(score.get("mode").toString());

        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://osu.ppy.sh/osu/" + bid + "&m=" + mode);
            conn = (HttpURLConnection) url.openConnection();
        } catch (Exception ignored) {

        }
        if(null != conn){
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE5.0; Windows NT; DigExt)");
            String osuFile = null;
            try {
                InputStream inputStream = conn.getInputStream();
                byte[] getData = readInputStream(inputStream);
                osuFile = new String(getData, "UTF-8");
            } catch (IOException ignored) {

            }
            if(!StringUtil.isNullOrEmpty(osuFile)){
                try{
                    StringReader reader = new StringReader(osuFile);
                    BufferedReader in = new BufferedReader(reader);
                    Koohii.Map map = new Koohii.Parser().map(in);
                    Koohii.DiffCalc stars = new Koohii.DiffCalc().calc(map, mods);
                    Koohii.PPv2Parameters p = new Koohii.PPv2Parameters();
                    p.beatmap = map;
                    p.aim_stars = stars.aim;
                    p.speed_stars = stars.speed;
                    p.mode = mode;
                    p.mods = mods;
                    p.n300 = n300;
                    p.n100 = n100;
                    p.n50 = n50;
                    p.nmiss = nmiss;
                    p.combo = combo;
                    Koohii.PPv2 pp = new Koohii.PPv2(p);
                    return pp.total;
                } catch (Exception ignored) {

                }
            }
        }
        return -1;
    }

    private static byte[] readInputStream(InputStream is) throws IOException{
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer,0,len);
        }

        bos.close();
        return bos.toByteArray();
    }

}
