package com.wxipad.wechat;

import java.nio.charset.Charset;

public class WechatConst {
    public static final String CHARSET_NAME = "UTF-8";
    public static final Charset CHARSET = Charset.forName(CHARSET_NAME);
    public static final String VERSION = "1.1.5";
    public static boolean DEBUG = true;//运行调试
    public static boolean DEVELOP = true;//开发调试
    public static boolean LOCAL = false;//本地处理
    public static boolean OFFLINE = false;//检测线下
    public static boolean PAYLOADS = false;//打印组包PAYLOADS


}
