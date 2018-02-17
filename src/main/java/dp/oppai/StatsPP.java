package dp.oppai;


import bot.dullwolf.common.utils.CmdUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class StatsPP {

    public static void stats(final Long id, final int type, final long bid, final String mods, final double acc){
        //final long id = System.currentTimeMillis();

        HttpURLConnection conn = null;

        try {
            URL url = new URL("https://osu.ppy.sh/osu/" + bid);
            conn = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            //e.printStackTrace();
            String format = String.format("下载歌谱时出现错误(%s)：\n%s", e.getClass().getName(), e.getMessage());
            CmdUtil.sendMessage(format,id,type);
        }

        assert conn != null;
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE5.0; Windows NT; DigExt)");

        try {
            InputStream inputStream = conn.getInputStream();
            byte[] getData = readInputStream(inputStream);
            File saveDir = new File("C:\\ppbot\\");

            if (!saveDir.exists()){
                saveDir.mkdirs();
            }

            File file = new File(saveDir.getCanonicalPath() + "\\" + bid + ".osu");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if(getData.length == 0){
                CmdUtil.sendMessage("无法获取该歌曲信息",id,type);
            }

            fos.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                OppaiCtx ctx = new OppaiCtx();
                Beatmap b = new Beatmap(ctx);

                int BUFFER_SIZE = 80000000;
                Buffer buf = new Buffer(BUFFER_SIZE);
                b.parse("C:\\ppbot\\" + bid + ".osu", buf, false, System.getProperty("user.dir"));

                if (checkError(ctx))
                    return;

                int mod = 0;
                if (!mods.equals("")){
                    mod = getMods(mods);
                }

                StringBuffer msg1 = new StringBuffer(b.toString());


                OppaiCtx dctx = new OppaiCtx(ctx); // Passing a ctx object will create a diff ctx object
                DiffCalc dCalcer = new DiffCalc();
                b.applyMods(mod);
                dCalcer.diffCalc(b, dctx, true, true, true, true);

                PPCalc ppCalcer = new PPCalc();
                ppCalcer.calcPP(b, ctx, dCalcer.getAim(), dCalcer.getSpeed());


                msg1.append(String.format("\nstar:%.2f ★ ",dCalcer.getStars()));

                if (mod != 0){
                    msg1.append("\nmod: ").append(mods);
                }else{
                    msg1.append("\nmod: None");
                }

                if(acc < 0) {
                    double pp1,pp2,pp3,pp4;
                    ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 95.0);
                    pp1 = ppCalcer.getPp();
                    ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 97.0);
                    pp2 = ppCalcer.getPp();
                    ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 99.0);
                    pp3 = ppCalcer.getPp();
                    ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 100.0);
                    pp4 = ppCalcer.getPp();

                    msg1.append(String.format("\nacc:95%% 可获得%dpp\nacc:97%% 可获得%dpp\nacc:99%% 可获得%dpp\nacc:100%% 可获得%dpp",Math.round(pp1),Math.round(pp2),Math.round(pp3),Math.round(pp4)));
                } else {
                    double pp;
                    ppCalcer.calcPPAcc(b,ctx,dCalcer.getAim(),dCalcer.getSpeed(),acc);
                    pp = ppCalcer.getPp();
                    msg1.append(String.format("\nacc:%.2f%% 可获得%dpp",acc,Math.round(pp)));
                }

                msg1.append("\n[注] 以上的数据有可能不准，仅供参考...还有就是，前提必须要FC！acc要在80%以上！");


                CmdUtil.sendMessage(msg1.toString(),id,type);

                //File file = new File("D:\\ppbot\\" + id + ".osu");
                //file.delete();

            }
        }).start();

    }


    public static String getPP(final long bid, final String mods, final double acc){
        //final long id = System.currentTimeMillis();

        HttpURLConnection conn;
        try {
            URL url = new URL("https://osu.ppy.sh/osu/" + bid);
            conn = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            //e.printStackTrace();
            return String.format("下载歌谱时出现错误(%s)：\n%s", e.getClass().getName(), e.getMessage());
        }
        assert conn != null;
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE5.0; Windows NT; DigExt)");

        try {
            InputStream inputStream = conn.getInputStream();
            byte[] getData = readInputStream(inputStream);
            File saveDir = new File("C:\\ppbot\\");

            if (!saveDir.exists()){
                saveDir.mkdirs();
            }

            File file = new File(saveDir.getCanonicalPath() + "\\" + bid + ".osu");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if(getData.length == 0){
                return "无法获取该歌曲信息";
            }

            fos.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OppaiCtx ctx = new OppaiCtx();
        Beatmap b = new Beatmap(ctx);

        int BUFFER_SIZE = 80000000;
        Buffer buf = new Buffer(BUFFER_SIZE);
        b.parse("C:\\ppbot\\" + bid + ".osu", buf, false, System.getProperty("user.dir"));

        if (checkError(ctx))
            return "获取PP出错";

        int mod = 0;
        if (!mods.equals("")){
            mod = getMods(mods);
        }

        StringBuffer msg1 = new StringBuffer(b.toString());


        OppaiCtx dctx = new OppaiCtx(ctx); // Passing a ctx object will create a diff ctx object
        DiffCalc dCalcer = new DiffCalc();
        b.applyMods(mod);
        dCalcer.diffCalc(b, dctx, true, true, true, true);

        PPCalc ppCalcer = new PPCalc();
        ppCalcer.calcPP(b, ctx, dCalcer.getAim(), dCalcer.getSpeed());


        msg1.append(String.format("\nstar:%.2f ★ ",dCalcer.getStars()));

        if (mod != 0){
            msg1.append("\nmod: ").append(mods);
        }else{
            msg1.append("\nmod: None");
        }

        double pp = 0;
        if(acc < 0) {
            double pp1,pp2,pp3,pp4;
            ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 95.0);
            pp1 = ppCalcer.getPp();
            ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 97.0);
            pp2 = ppCalcer.getPp();
            ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 99.0);
            pp3 = ppCalcer.getPp();
            ppCalcer.calcPPAcc(b, ctx, dCalcer.getAim(), dCalcer.getSpeed(), 100.0);
            pp4 = ppCalcer.getPp();
            msg1.append(String.format("\nacc:95%% 可获得%dpp\nacc:97%% 可获得%dpp\nacc:99%% 可获得%dpp\nacc:100%% 可获得%dpp",Math.round(pp1),Math.round(pp2),Math.round(pp3),Math.round(pp4)));
        } else {
            ppCalcer.calcPPAcc(b,ctx,dCalcer.getAim(),dCalcer.getSpeed(),acc);
            pp = ppCalcer.getPp();
            msg1.append(String.format("\nacc:%.2f%% 可获得%dpp",acc,Math.round(pp)));
        }

        msg1.append("\n[注] 以上的数据有可能不准，仅供参考...还有就是，前提必须要FC！acc要在80%以上！");
        return Math.round(pp)+"";

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

    public static boolean checkError(OppaiCtx ctx) {
        String err = ctx.getLastErr();

        if(!err.equals("")) {
            System.out.println(err);
            return true;
        }
        return false;
    }

    public static int getMods(String mods){
        mods = mods.toUpperCase();

        int mod = 0;

        if (mods.contains("NONE"))
            return mod;
        if (mods.contains("EZ"))
            mod = mod | Mods.EZ;
        if (mods.contains("NF"))
            mod = mod | Mods.NF;
        if (mods.contains("HT"))
            mod = mod | Mods.HT;
        if (mods.contains("HR"))
            mod = mod | Mods.HR;
        //SD and PF are 0.
        if (mods.contains("DT"))
            mod = mod | Mods.DT;
        if (mods.contains("NC"))
            mod = mod | Mods.NC;
        if (mods.contains("HD"))
            mod = mod | Mods.HD;
        if (mods.contains("FL"))
            mod = mod | Mods.FL;
        if (mods.contains("SO"))
            mod = mod | Mods.SO;

        return mod;
    }
}
