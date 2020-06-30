package com.wxipad.wechat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wxipad.client.CalculationService;
import com.wxipad.proto.User;
import com.wxipad.web.ApiCmd;
import com.wxipad.web.ApplicationRunnerImpl;
import com.wxipad.web.RedisUtils;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import com.wxipad.wechat.tools.model.WechatReturn;
import com.wxipad.wechat.tools.uitls.WechatUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static com.wxipad.wechat.WechatApi.setWechatReturn;
import static com.wxipad.wechat.tools.uitls.WechatUtil.getMd5;

@Data
@Slf4j
public abstract class WechatExample extends Thread {

    private static final Logger log = Logger.getLogger(WechatExample.class.getName());

    public static WechatReturn wechatReturn;
    public static final String redisk_key_loinged_user = "robot_logined_users";
    public static String ACCOUNT = "ming";
    public static final String PATH = System.getProperty("user.dir") + "/";//当前路径
    public static final int TYPE = WechatIns.TYPE_IPAD;//登录微信类型
    public static String CONFIG = PATH + "config.json";
    public static final int TEST_CLIENT = 3;
    public static final String FILE = PATH + "auto.txt";//自动登录文件
    public static final String FILE_TOKEN = FILE + ".key";//自动登录文件TOKEN
    public static final String FILE_DATA = FILE + ".dat";//自动登录文件DATA
    public static final String IMAGE = PATH + "baby.jpg";
    public static final int TEST_API = 2;
    public static final int TEST_INS = 1;
    public static String serverId;//服务 id
    public static byte[] redisId;//登录设备名称
    public static byte[] qrCode;//二维码图片
    public static int serverPort;//服务
    public static int protocolVer;//登录设备名称
    public static String randomId;//微信号操作ID
    public static String account;//微信号拥有者 ID
    public static String userA16;
    public static String userName;
    public static String nickName;
    public static String userWxDat;
    public static String softwareId;
    public static String longServerHost;
    public static String shortServerHost;
    public static String config;//配置文件
    public static String serverIp;//服务 ip
    public static String serverHost;//服务 地址
    public static String devideId;//登录设备 唯一ID
    public static String deviceMac;//登录设备 网卡号
    public static String deviceUuid;//登录设备 uuid
    public static String deviceName;//登录设备名称
    public static String randomSeeds;//微信实例随机种子字符串
    public static User.Builder loginedUserBuilder;
    public static WechatApiMsg wechatApiMsg;
    protected ScheduledExecutorService heartBeatExe = Executors.newSingleThreadScheduledExecutor();
    protected ScheduledExecutorService isAlifeCheckSevice = Executors.newSingleThreadScheduledExecutor();
    private String deviceType;
    private boolean dead;

    public WechatExample(WechatApiMsg apiMsg) throws InvalidProtocolBufferException {
        wechatApiMsg = apiMsg;
        account = apiMsg.account;
        randomId = apiMsg.randomId;
        ACCOUNT = account + randomId;
        protocolVer = apiMsg.protocolVer;
        config = ApplicationRunnerImpl.config;
        redisId = ApplicationRunnerImpl.redisId;
        serverId = ApplicationRunnerImpl.serverId;
        serverIp = ApplicationRunnerImpl.serverIp;
        serverPort = ApplicationRunnerImpl.serverPort;
        serverHost = ApplicationRunnerImpl.serverHost;
        randomSeeds = WechatUtil.getMd5(account + randomId);
        deviceName = WechatUtil.getDevideName();
        if (randomId == null || randomId.isEmpty()) {
            randomId = WechatTool.randomUUID();
            log.info("微信实例 randomId:[{}]", randomId);
            wechatApiMsg.randomId = randomId;
        }

        WechatClient.startup(null, config);
        userInit();
    }

    public static void userInit() throws InvalidProtocolBufferException {
        wechatApiMsg = WechatApi.create(TYPE, new WechatApi.Callback() {
            @Override
            public void online(WechatIns ins) {
                log.info("wechat online ...");
            }

            @Override
            public void offline(WechatIns ins) {
                log.info("wechat offline ...");
            }

            @Override
            public void privateText(WechatIns ins, String from_user, String to_user, long timestamp, String content, boolean self) {
                log.info((self ? "自己" : "他人") + "私聊文字：" + content);
            }

            @Override
            public void groupText(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, String[] atlist, boolean self) {
                log.info((self ? "自己" : "他人") + "组群文字：" + content);
                if (self) {
                    try {
                        if ("1".equals(content)) {
                            WechatApi.sendText(ins.id, from_group, "Hello World");
                        } else if ("2".equals(content)) {
                            WechatApi.sendImage(ins.id, from_group, IMAGE);
                        } else if ("3".equals(content)) {
                            //无CDN发图
                        } else if ("4".equals(content)) {
                            WechatApi.SnsObj snsObj = WechatApi.snsSend(ins.id, "Hello World");
                            WechatApi.snsComment(ins.id, snsObj.id, snsObj.userName, "COMMENT");
                        } else if ("5".equals(content)) {
                            WechatApi.snsSend(ins.id, "Hello World", new String[]{IMAGE});
                        } else if ("6".equals(content)) {
                            WechatApi.snsSend(ins.id, null, new String[]{IMAGE});
                        } else if ("7".equals(content)) {
                            String wxid = WechatApi.self(ins.id);
                            WechatApi.GetQrcode result = WechatApi.getQrcode(ins.id, wxid, 0);
                            if (result != null) {
                                WechatTool.writeFile(PATH + "wxid.png", result.qrcode);
                            }
                        } else if ("8".equals(content)) {
                            ArrayList<WechatApi.GroupMember> members = WechatApi.getChatRoomMember(ins.id, from_group);
                            if (members != null) {
                                for (WechatApi.GroupMember member : members) {
                                    log.info(member.userName + "(" + member.nickName + ") - " + member.inviteUser);
                                }
                                log.info("共计:" + members.size());
                            }
                        } else if ("9".equals(content)) {
                            WechatApi.snsComment(ins.id, "13086011153780117669",
                                    "wxid_2i58ze8ywian22", "评论:" + System.currentTimeMillis());
                        } else if (content.startsWith("踢") && atlist != null) {
                            for (String at : atlist) {
                                WechatApi.deleteChatRoomMember(ins.id, from_group, at);
                            }
                        } else if (content.startsWith("加")) {
                            //中文"加"后跟wxid可以搜索微信号添加好友
                            WechatApi.ContactSearch result = WechatApi.searchUser(ins.id, content);
                            if (result != null && result.v1 != null && result.v2 != null) {
                                WechatApi.addUser(ins.id, result.v1, result.v2, 3,
                                        "Hello - " + WechatTool.bytesToHex(WechatTool.randomBytes(2)));
                            }
                        }
                    } catch (WechatApi.ApiException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void privateImage(WechatIns ins, String from_user, String to_user, long timestamp, String content, byte[] data, boolean self) {
                log.info((self ? "自己" : "他人") + "私聊图片：" + content);
                WechatTool.writeFile(PATH + "temp.png", data);
                try {
                    WechatApi.sendRecvImage(ins.id, from_user, content);
                } catch (WechatApi.ApiException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void groupImage(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, byte[] data, boolean self) {
                log.info((self ? "自己" : "他人") + "组群图片：" + content);
                WechatTool.writeFile(PATH + "temp.png", data);
            }

            @Override
            public void privateVideo(WechatIns ins, String from_user, String to_user, long timestamp, String content, boolean self) {
                log.info((self ? "自己" : "他人") + "私聊视频：" + content);
                try {
                    WechatApi.sendRecvVideo(ins.id, from_user, content);
                } catch (WechatApi.ApiException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void groupVideo(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, boolean self) {
                log.info((self ? "自己" : "他人") + "组群视频：" + content);
            }

            @Override
            public void groupInvite(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String nick_name) {
                log.info("邀请：" + nick_name);
                try {
                    WechatApi.sendText(ins.id, from_group, "欢迎新成员【" + nick_name + "】的加入！~");
                } catch (WechatApi.ApiException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void friendAdd(WechatIns ins, String from_user, String v1, String v2, long timestamp, String remark, String source_user) {
                log.info("好友请求v1：" + v1);
                log.info("好友请求v2：" + v2);
                try {
                    WechatApi.acceptUser(ins.id, v1, v2);
                } catch (WechatApi.ApiException e) {
                    e.printStackTrace();
                }
            }
        }, wechatApiMsg);
    }

    public static WechatReturn getStater() {
        long timeStamp = System.currentTimeMillis() / 1000;
        if (wechatReturn.retdata == null) {
            wechatReturn.retdata(GsonUtil.GsonString(wechatApiMsg));
        }
        return wechatReturn.timeStamp((int) timeStamp)
                .wechatApiId(getMd5(account + timeStamp))
                .result(true)
                .cmd(wechatApiMsg.getCmd());
    }


    public static void sendMsg(Object data) {
        WechatApi.sendMsg(wechatApiMsg, data);
    }


    public static void getQrcode(int cmd) throws Exception {
        try {
            WechatApi.init(randomId, deviceName, ACCOUNT);
            WechatObj.LoginQrcode getLoginQrcode = WechatApi.getLoginQrcode(randomId);
            getLoginQrcode.ImgBufUrl = "http://weixin.qq.com/x/" + getLoginQrcode.Uuid;
            //getLoginQrcode.ImgBuf = "";
            WechatApi.GetLoginQrcode rtn = new WechatApi.GetLoginQrcode();
            rtn.qrcode = Base64.getDecoder().decode(getLoginQrcode.ImgBuf);
            if (getLoginQrcode == null || rtn.qrcode == null) {
                log.info("获取登录二维码失败...");
                wechatReturn = setWechatReturn(wechatApiMsg, -1, "获取登录二维码失败...", null, ApiCmd.LOGIN_QR_GET);
                sendMsg(wechatReturn);
                return;
            }
            wechatReturn = setWechatReturn(wechatApiMsg, 1, "获取二维码成功!请打开手机App端,用摄像头扫描二维码!", getLoginQrcode, ApiCmd.LOGIN_QR_GET);
            sendMsg(wechatReturn);
            boolean success = false;
            WechatTool.writeFile(PATH + "qrcode.png", rtn.qrcode);
            if (success) {
                success = WechatApi.online(randomId, 60 * 1000l);//最大等待1分钟登录成功
            }
            WechatObj.LoginQrcode checkLoginQrcode = WechatApi.checkLoginQrcode(randomId);
            checkLoginQrcode.Uuid = getLoginQrcode.Uuid;
            checkLoginQrcode.ImgBuf = "";
            checkLoginQrcode.ImgBufUrl = getLoginQrcode.ImgBufUrl;
            int expiry = 240;
            while (checkLoginQrcode != null) {
                expiry--;
                int status = checkLoginQrcode.Status;
                if (status == 1 || status == 0) {
                    checkLoginQrcode.ExpiredTime = expiry;
                    if (status == 0) {
                        checkLoginQrcode.ImgBuf = getLoginQrcode.ImgBuf;
                        log.info("实例:[{}] 状态码:[{}] 获取二维码成功!请打开手机App端,用摄像头扫描二维码!剩余时间:[{}]", randomId, status, checkLoginQrcode.ExpiredTime);
                        wechatReturn = setWechatReturn(wechatApiMsg, status, "获取二维码成功!请打开手机App端,用摄像头扫描二维码,并点击\"确认\"进行登录", checkLoginQrcode, ApiCmd.CGI_CHECK_LOGIN);
                    } else {
                        log.info("实例:[{}] 状态码:[{}] 昵称:[{}] 头像:[{}]  扫描二维码成功!请点击\"确认\"进行登录!剩余时间:[{}]", randomId, status, checkLoginQrcode.Nickname, checkLoginQrcode.HeadImgUrl, checkLoginQrcode.ExpiredTime);
                        wechatReturn = setWechatReturn(wechatApiMsg, status, "扫描二维码成功!请点击\"确认\"进行登录", checkLoginQrcode, ApiCmd.CGI_CHECK_LOGIN);
                        sendMsg(wechatReturn);
                    }
                    WechatTool.delay(1000L);
                    checkLoginQrcode = WechatApi.checkLoginQrcode(randomId);
                } else if (status == 2) {
                    log.info("实例:[{}] 状态码:[{}] 昵称:[{}] 微信ID:[{}] 头像:[{}]  已在手机端点击\"确认\"正在进行登录.....", randomId, status, checkLoginQrcode.Nickname, checkLoginQrcode.Username, checkLoginQrcode.HeadImgUrl);
                    wechatReturn = setWechatReturn(wechatApiMsg, status, "已在手机端点击\"确认\",正在进行登录...", checkLoginQrcode, ApiCmd.CGI_CHECK_LOGIN);
                    sendMsg(wechatReturn);
                    success = WechatApi.manualAuth(ApiCmd.CGI_QRCODE_LOGIN, randomId);
                    break;
                } else {
                    wechatReturn = setWechatReturn(wechatApiMsg, status, "登录失败!请重新获取二维码!", checkLoginQrcode, ApiCmd.CGI_CHECK_LOGIN);
                    log.info("状态码:[" + status + "],退出...");
                    sendMsg(wechatReturn);
                    break;
                }
            }
            Login(ApiCmd.CGI_QRCODE_LOGIN, success);
        } finally {
            WechatApi.dispose(randomId);
        }
    }

    public static void userLogin() throws Exception {
        boolean success = false;
        success = WechatApi.manualAuth(ApiCmd.LOGIN_A62, randomId);
        Login(ApiCmd.LOGIN_A62, success);
    }

    public static void a16Login() throws Exception {
        boolean success = false;
        success = WechatApi.manualAuth(ApiCmd.LOGIN_A16, randomId);
        Login(ApiCmd.LOGIN_A16, success);
    }


    public static void autoLogin() throws Exception {
        try {
            WechatApi.init(randomId, deviceName, ACCOUNT);
            boolean success = false;
            //自动登录
            String token = WechatTool.readFile(FILE_TOKEN, WechatConst.CHARSET);
            String data = WechatTool.readFile(FILE_DATA, WechatConst.CHARSET);
            WechatApi.setAutoLoginPack(randomId, token, data);
            success = WechatApi.autoAuth(randomId);


            if (success) {
                success = WechatApi.online(randomId, 60 * 1000l);//最大等待1分钟登录成功
            }
            Login(ApiCmd.LOGIN_ONE, success);
        } finally {
            WechatApi.dispose(randomId);
        }
    }

    public static void wxDataLogin() throws Exception {
        try {
            WechatApi.init(randomId, deviceName, ACCOUNT);
            boolean success = false;
            //自动登录
            String token = WechatTool.readFile(FILE_TOKEN, WechatConst.CHARSET);
            String data = WechatTool.readFile(FILE_DATA, WechatConst.CHARSET);
            WechatApi.setAutoLoginPack(randomId, token, data);
            success = WechatApi.autoAuth(randomId);
            if (success) {
                success = WechatApi.online(randomId, 60 * 1000l);//最大等待1分钟登录成功
            }
            Login(ApiCmd.LOGIN_A62, success);
        } finally {
            WechatApi.dispose(randomId);
        }
    }


    public static void Login(int cmd, boolean success) throws Exception {
        if (success) {
            log.info("微信登录成功...");
            WechatApi.AutoLoginPack pack = WechatApi.getAutoLoginPack(randomId);
            wechatReturn = setWechatReturn(wechatApiMsg, 1, "", null, cmd);
            sendMsg(wechatReturn);




            WechatTool.writeFile(FILE_TOKEN, pack.getToken(), WechatConst.CHARSET);
            WechatTool.writeFile(FILE_DATA, pack.getData(), WechatConst.CHARSET);
            while (WechatApi.online(randomId)) {
                WechatTool.delay(1000l);
            }
            if (WechatTool.existFile(FILE_TOKEN)) {
                WechatTool.deleteFile(FILE_TOKEN);
            }
            if (WechatTool.existFile(FILE_DATA)) {
                WechatTool.deleteFile(FILE_DATA);
            }
            log.info("微信已退出...");
        } else {
            log.info("微信登录失败...");
            setWechatReturn(wechatApiMsg, -1, "登录失败!请重新登录!", null, cmd);

        }
    }



    public static void loadUsertest(int test) throws Exception {
        config = ApplicationRunnerImpl.config;
        if (test == TEST_CLIENT) {
            log.info("开始创建二维码");
            testClient(config);
        } else {
            WechatObj.GrpcConfig obj = WechatTool.gsonObj(config, WechatObj.GrpcConfig.class);
            WechatObj.GrpcConfig.init(obj);
            try {
                if (test == TEST_API) {
                    testApi();
                } else if (test == TEST_INS) {
                    testIns();
                } else {
                    WechatTool.echo("未知的测试程序 ...");
                }
            } finally {
                WechatObj.GrpcConfig.dispose();
            }
        }
    }

    public static void testClient(String config) throws Exception {
        WechatClient.startup(null, config);
        randomId = WechatClient.create(TYPE, new WechatClient.Callback() {
            @Override
            public void online(String id) {
                log.info("wechat 上线 ...");
            }
            @Override
            public void offline(String id) {
                log.info("wechat offline ...");
            }
            @Override
            public void privateText(String id, String from_user, String to_user, long timestamp, String content, boolean self) {
                log.info((self ? "自己" : "他人") + "私聊文字：" + content);
            }
            @Override
            public void groupText(String id, String from_group, String from_user, String to_user, long timestamp, String content, String[] atlist, boolean self) {
                log.info((self ? "自己" : "他人") + "组群文字：" + content);
                if (self) {
                    if ("1".equals(content)) {
                        WechatClient.sendText(id, from_group, "Hello World");
                    } else if ("2".equals(content)) {
                        WechatClient.sendImage(id, from_group, IMAGE);
                    } else if ("3".equals(content)) {
                        WechatClient.sendCdnImage(id, from_group, IMAGE);
                    } else if ("4".equals(content)) {
                        WechatClient.SnsObj snsObj = WechatClient.snsSend(id, "Hello World");
                        WechatClient.snsComment(id, snsObj.id, snsObj.userName, "COMMENT");
                    } else if ("5".equals(content)) {
                        WechatClient.snsSend(id, "Hello World", new String[]{IMAGE});
                    } else if ("6".equals(content)) {
                        WechatClient.snsSend(id, null, new String[]{IMAGE});
                    } else if ("7".equals(content)) {
                        String wxid = WechatClient.self(id);
                        WechatClient.GetQrcode result = WechatClient.getQrcode(id, wxid, 0);
                        if (result != null) {
                            WechatTool.writeFile(PATH + "wxid.png", result.qrcode);
                        }
                    } else if ("8".equals(content)) {
                        ArrayList<WechatClient.GroupMember> members = WechatClient.getChatRoomMember(id, from_group);
                        if (members != null) {
                            for (WechatClient.GroupMember member : members) {
                                log.info(member.userName + "(" + member.nickName + ") - " + member.inviteUser);
                            }
                            log.info("total:" + members.size());
                        }
                    } else if ("9".equals(content)) {
                        WechatClient.setChatRoomAnnouncement(id, from_group, "Hello World");
                    } else if (content.startsWith("踢") && atlist != null) {
                        for (String at : atlist) {
                            WechatClient.deleteChatRoomMember(id, from_group, at);
                        }
                    } else if (content.startsWith("加")) {
                        //中文"加"后跟wxid可以搜索微信号添加好友
                        WechatClient.ContactSearch result = WechatClient.searchUser(id, content);
                        if (result != null && result.v1 != null && result.v2 != null) {
                            WechatClient.addUser(id, result.v1, result.v2, 3,
                                    "Hello - " + WechatTool.bytesToHex(WechatTool.randomBytes(2)));
                        }
                    }
                }
            }
            @Override
            public void privateImage(String id, String from_user, String to_user, long timestamp, String content, byte[] data, boolean self) {
                log.info((self ? "自己" : "他人") + "私聊图片：" + content);
                WechatTool.writeFile(PATH + "temp.png", data);
                WechatClient.sendRecvImage(id, from_user, content);
            }
            @Override
            public void groupImage(String id, String from_group, String from_user, String to_user, long timestamp, String content, byte[] data, boolean self) {
                log.info((self ? "自己" : "他人") + "组群图片：" + content);
                WechatTool.writeFile(PATH + "temp.png", data);
            }
            @Override
            public void privateVideo(String id, String from_user, String to_user, long timestamp, String content, boolean self) {
                log.info((self ? "自己" : "他人") + "私聊视频：" + content);
                WechatClient.sendRecvVideo(id, from_user, content);
            }
            @Override
            public void groupVideo(String id, String from_group, String from_user, String to_user, long timestamp, String content, boolean self) {
                log.info((self ? "自己" : "他人") + "组群视频：" + content);
            }
            @Override
            public void groupInvite(String id, String from_group, String from_user, String to_user, long timestamp, String nick_name) {
                log.info("邀请：" + nick_name);
                WechatClient.sendText(id, from_group, "欢迎新成员【" + nick_name + "】的加入！~");
            }
            @Override
            public void friendAdd(String id, String from_user, String v1, String v2, long timestamp, String remark, String source_user) {
                log.info("好友请求v1：" + v1);
                log.info("好友请求v2：" + v2);
                WechatClient.acceptUser(id, v1, v2);
            }
        });
        log.info("微信实例 randomId:[{}]", randomId);

        WechatClient.init(randomId, deviceName, null, ACCOUNT);
        try {
            boolean success = false;
            if (WechatTool.existFile(FILE_TOKEN) && WechatTool.existFile(FILE_DATA)) {
                //自动登录
                String token = WechatTool.readFile(FILE_TOKEN, WechatConst.CHARSET);
                String data = WechatTool.readFile(FILE_DATA, WechatConst.CHARSET);
                WechatClient.setAutoLoginPack(randomId, token, data);
                success = WechatClient.autoAuth(randomId);
            } else {
                //扫码登录
                WechatClient.GetLoginQrcode getLoginQrcode = WechatClient.getLoginQrcode(randomId);
                if (getLoginQrcode == null || getLoginQrcode.qrcode == null) {
                    log.info("获取登录二维码失败...");
                    return;
                }
                qrCode = getLoginQrcode.qrcode;
                WechatTool.writeFile(PATH + "qrcode.png", qrCode);

                WechatClient.CheckLoginQrcode checkLoginQrcode = WechatClient.checkLoginQrcode(randomId);
                int expiry = 240;
                while (checkLoginQrcode != null) {
                    if (checkLoginQrcode.status == 0 || checkLoginQrcode.status == 1) {
                        WechatTool.delay(1000);
                        expiry--;
                        checkLoginQrcode.expiry = expiry;
                        log.info("实例:[{}] 状态码:[{}] 昵称:[{}] 剩余时间:[{}]", randomId, checkLoginQrcode.status, checkLoginQrcode.nickName, checkLoginQrcode.expiry);
                        checkLoginQrcode = WechatClient.checkLoginQrcode(randomId);
                    } else if (checkLoginQrcode.status == 2) {
                        success = WechatClient.manualAuth(randomId);
                        break;
                    } else {
                        log.info("状态码:[{}],退出...", checkLoginQrcode.status);
                        break;
                    }
                }
            }
            if (success) {
                success = WechatClient.online(randomId, 60 * 1000l);//最大等待1分钟登录成功
            }
            if (success) {
                log.info("微信登录成功...");
                WechatClient.AutoLoginPack pack = WechatClient.getAutoLoginPack(randomId);
                WechatTool.writeFile(FILE_TOKEN, pack.getToken(), WechatConst.CHARSET);
                WechatTool.writeFile(FILE_DATA, pack.getData(), WechatConst.CHARSET);
                while (WechatClient.online(randomId)) {
                    WechatTool.delay(1000l);
                }
                if (WechatTool.existFile(FILE_TOKEN)) {
                    WechatTool.deleteFile(FILE_TOKEN);
                }
                if (WechatTool.existFile(FILE_DATA)) {
                    WechatTool.deleteFile(FILE_DATA);
                }
                log.info("微信已退出...");
            } else {
                log.info("微信登录失败...");
            }
        } finally {
            WechatClient.dispose(randomId);
        }
        WechatClient.shutdown(null);
    }


    public static void testApi() throws Exception {
        userInit();
        try {
            WechatApi.init(randomId, deviceName, ACCOUNT);
            boolean success = false;
            if (WechatTool.existFile(FILE_TOKEN) && WechatTool.existFile(FILE_DATA)) {
                //自动登录
                String token = WechatTool.readFile(FILE_TOKEN, WechatConst.CHARSET);
                String data = WechatTool.readFile(FILE_DATA, WechatConst.CHARSET);
                WechatApi.setAutoLoginPack(randomId, token, data);
                success = WechatApi.autoAuth(randomId);
            } else {
                WechatApi.init(randomId, deviceName, ACCOUNT);
                WechatObj.LoginQrcode getLoginQrcode = WechatApi.getLoginQrcode(randomId);
                getLoginQrcode.ImgBufUrl = "http://weixin.qq.com/x/" + getLoginQrcode.Uuid;
                WechatApi.GetLoginQrcode rtn = new WechatApi.GetLoginQrcode();
                rtn.qrcode = Base64.getDecoder().decode(getLoginQrcode.ImgBuf);
                //扫码登录
                if (rtn.qrcode == null) {
                    log.info("获取登录二维码失败...");
                    return;
                }
                qrCode = rtn.qrcode;
                WechatTool.writeFile(PATH + "qrcode.png", qrCode);
                WechatObj.LoginQrcode checkLoginQrcode = WechatApi.checkLoginQrcode(randomId);
                checkLoginQrcode.ImgBufUrl = "http://weixin.qq.com/x/" + checkLoginQrcode.Uuid;
                int expiry = 240;
                while (checkLoginQrcode != null) {
                    if (checkLoginQrcode.Status == 0 || checkLoginQrcode.Status == 1) {
                        WechatTool.delay(1000l);
                        expiry--;
                        checkLoginQrcode.ExpiredTime = expiry;
                        log.info("实例:[{}] 状态码:[{}] 昵称:[{}] 剩余时间:[{}]", randomId, checkLoginQrcode.Status, checkLoginQrcode.Nickname, checkLoginQrcode.ExpiredTime);
                        checkLoginQrcode = WechatApi.checkLoginQrcode(randomId);
                    } else if (checkLoginQrcode.Status == 2) {
                        success = WechatApi.manualAuth(1111, randomId);
                        break;
                    } else {
                        log.info("状态码:[" + checkLoginQrcode.Status + "],退出...");
                        break;
                    }
                }
            }
            if (success) {
                success = WechatApi.online(randomId, 60 * 1000l);//最大等待1分钟登录成功
            }
            if (success) {
                log.info("微信登录成功...");
                WechatApi.AutoLoginPack pack = WechatApi.getAutoLoginPack(randomId);
                WechatTool.writeFile(FILE_TOKEN, pack.getToken(), WechatConst.CHARSET);
                WechatTool.writeFile(FILE_DATA, pack.getData(), WechatConst.CHARSET);
                while (WechatApi.online(randomId)) {
                    WechatTool.delay(1000l);
                }
                if (WechatTool.existFile(FILE_TOKEN)) {
                    WechatTool.deleteFile(FILE_TOKEN);
                }
                if (WechatTool.existFile(FILE_DATA)) {
                    WechatTool.deleteFile(FILE_DATA);
                }
                log.info("微信已退出...");
            } else {
                log.info("微信登录失败...");
            }
        } finally {
            WechatApi.dispose(randomId);
        }
    }

    public static void testIns() throws Exception {
        //初始化微信实例
        WechatIns ins = new WechatIns(WechatIns.TYPE_IPAD, new WechatIns.Callback() {

            @Override
            public void syncMessage(WechatIns ins, WechatObj.Message msg) {
                long timestamp = msg.CreateTime * 1000l;
                if (ins.online.get() <= 0 || timestamp < ins.online.get()) {
                    return;//跳过登录前的消息
                }
                log.info("Message[" + msg.MsgType + "]:" + msg.Content);
                String username = ins.isSelf(msg.FromUserName) ? msg.ToUserName : msg.FromUserName;
                if ("你好".equals(msg.Content)) {
                    ins.sendMicroMsg(username, "你也好");
                } else if ("时间".equals(msg.Content)) {
                    ins.sendMicroMsg(username, new Date().toString());
                } else if ("1".equals(msg.Content)) {
                    ins.sendMicroMsg(username, "Hello World");
                } else if ("2".equals(msg.Content)) {
                    byte[] image = WechatTool.readFile(IMAGE);
                    ins.sendImageMsg(username, image);
                } else if ("3".equals(msg.Content)) {
                    WechatObj.ContactInfo info1 = ins.searchContact("xgnimcn");
                    log.info("message1:" + info1.NickName);
                    if (info1.UserName != null && info1.UserName.startsWith("v1_")
                            && info1.Ticket != null && info1.Ticket.startsWith("v2_")) {
                        ins.addContact(info1.UserName, info1.Ticket, 2, 3,
                                "Hello World - " + System.currentTimeMillis());
                    }
                    WechatObj.ContactInfo info2 = ins.searchContact("加 [wxid_2i58ze8ywian22]");
                    log.info("信息2:" + info2.NickName);
                } else if ("4".equals(msg.Content)) {
                    WechatObj.QrcodeInfo info = ins.getQrcode(ins.getUserName());
                    byte[] data = Base64.getDecoder().decode(info.QrcodeBuf);
                    WechatTool.writeFile(PATH + "wxid.png", data);
                } else if ("5".equals(msg.Content)) {
                    if (ins.isGroup(username)) {
                        log.info("Is Group");
                        WechatObj.ChatroomInfo info = ins.getChatroomInfo(username);
                        log.info("info:" + info.ChatroomUsername);
                        if (info != null && info.MemberDetails != null) {
                            for (WechatObj.ChatroomMember member : info.MemberDetails) {
                                log.info(member.Username
                                        + " - " + member.NickName
                                        + " - " + member.InviterUserName);
                                if ("删".equals(member.DisplayName)) {
                                    boolean del = ins.delChatroomMember(username, member.Username);
                                    log.info("[" + member.NickName + "] 删除" + (del ? "成功" : "失败"));
                                }
                            }
                        }
                    } else {
                        log.info("Not Group");
                    }
                } else if ("6".equals(msg.Content)) {
                    ins.snsPost("Hello World");
                } else if ("7".equals(msg.Content)) {
                    byte[] image = WechatTool.readFile(IMAGE);
                    ins.snsUploadImage(image);
                } else if ("8".equals(msg.Content)) {
                    try {
                        //使用本地加解密发送图片和文字
                        ins.shortConn().sendSendMsg(username, "这是一段回复的消息", null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if ("9".equals(msg.Content)) {
                    log.info("doLogout - " + ins.doLogout());
                }
            }

            @Override
            public void online(WechatIns ins) {
                log.info("Wechat Online ...");
                String message = "Hello World - " + new Date().toString();
                ins.sendMicroMsg(ins.getUserName(), message);
            }

            @Override
            public void offline(WechatIns ins) {
                log.info("Wechat Offline ...");
            }

        }, wechatApiMsg).init(ACCOUNT);
        try {
            boolean success = false, fetched = false;
            if (WechatTool.existFile(FILE)) {
                String json = WechatTool.readFile(FILE, WechatConst.CHARSET);
                WechatObj.AutoPack pack = WechatTool.gsonObj(json, WechatObj.AutoPack.class);
                ins.setAutoPack(pack);
                success = ins.doLoginAuto();
            } else {
                WechatObj.LoginQrcode obj = ins.getLoginQrcode();
                byte[] data = Base64.getDecoder().decode(obj.ImgBuf);
                qrCode = data;
                WechatTool.writeFile(PATH + "qrcode.png", data);
                WechatObj.LoginQrcode obj2 = ins.checkLoginQrcode();
                while (obj2 != null) {
                    log.info(obj2.Status + " - " + obj2.Nickname + " - " + obj2.HeadImgUrl);
                    if (obj2.Status == 0 || obj2.Status == 1) {
                        WechatTool.delay(1 * 1000l);
                    } else if (obj2.Status == 2) {
                        success = ins.doLoginQrcode();
                        break;
                    } else {
                        break;//其他状态
                    }
                    obj2 = ins.checkLoginQrcode();
                }
            }
            log.info("login - " + success);
            while (success && ins.logined() && ins.online()) {
                if (!fetched && ins.logined() && ins.online()) {
                    WechatObj.AutoPack pack = ins.getAutoPack();
                    String json = WechatTool.gsonString(pack);
                    WechatTool.writeFile(FILE, json, WechatConst.CHARSET);
                    fetched = true;
                }
                WechatTool.delay(5 * 1000l);
            }
            if (success && ins.logined() && !ins.online()) {
                if (WechatTool.existFile(FILE)) {
                    WechatTool.deleteFile(FILE);
                }
            }
            log.info("the end ...");
        } finally {
            ins.dispose();
        }
    }

    abstract public void setOffline(boolean dead, int ss);

    public void exits(String id) {
        log.info("------用户[{}]-[{}]-[{}]-[{}]离线,线程将在两分钟后终止!------", account, randomId, userName, nickName);
        try {
            RedisUtils.hrem(redisId, randomId.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!dead) {
            dead = true;
        }
    }

    public void exit() {
        exits(randomId);
        heartBeatExe.shutdown();
        isAlifeCheckSevice.shutdown();
        try {
            //离线后，等待2分钟终止线程，资源回收。
            Thread.sleep(1800000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("account:[{}],randomid:[{}],userName:[{}],nickName:[{}]离线,线程终止!", account, randomId, userName, nickName);
        this.interrupt();
    }

    public void sendTextMsg(String targetWxId, String content) throws Exception{
        WechatApi.sendText(randomId, targetWxId, content);
    }



}



