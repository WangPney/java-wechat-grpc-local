package com.wxipad.wechat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WechatDebug {

    private static final WechatDebug lock = new WechatDebug();

    public static void echo(String msg, boolean noBreak) {
        if (WechatConst.DEVELOP) {
            if (noBreak) {
                System.out.print(msg);
            } else {
                System.out.println(msg);
            }
        }
    }

    public static void echo(String... msgs) {
        if (WechatConst.DEVELOP) {
            for (String msg : msgs) {
                System.out.println(msg);
            }
        }
    }

    public static void echo(Exception e) {
        if (WechatConst.DEVELOP) {
            e.printStackTrace();
        }
        if (WechatConst.DEBUG) {
            log("error.txt", e.getClass().getName(), e.getMessage());
        }
    }

    public static void log(WechatCtx ctx, String func, String... msgs) {
        if (WechatConst.DEBUG) {
            String wxid = ctx.getUsername();
            logss(wxid, func, msgs);
        }
    }


    public static void logss(String wxid, String func, String... msgs) {
        if (wxid != null) {
            String dirPath = "." + File.separator + "wxlogs";
            File dirFile = new File(dirPath);
            if (!dirFile.exists() && !dirFile.mkdirs()) {
                return;//日志目录创建失败
            }
            log(dirPath + File.separator + wxid + ".txt", func, msgs);
        }
    }

    public static void log(WechatIns ins, String func, String... msgs) {
        if (WechatConst.DEBUG) {
            String wxid = ins.getUserName();
            logss(wxid, func, msgs);
        }
    }

    public static void log(String path, String func, String... msgs) {
        File file = new File(path);
        OutputStream out = null;
        synchronized (lock) {
            try {
                if (file.exists()) {
                    out = new FileOutputStream(file, true);
                } else {
                    out = new FileOutputStream(file);
                }
                try {
                    for (String msg : msgs) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = format.format(new Date(System.currentTimeMillis()));
                        String line = "[" + time + "]" + func + "@" + msg + "\r\n";
                        out.write(line.getBytes(WechatConst.CHARSET));
                    }
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String bytes(byte[] data) {
        if (data == null) {
            return "null";
        } else {
            return "[" + data.length + "]";
        }
    }

}
