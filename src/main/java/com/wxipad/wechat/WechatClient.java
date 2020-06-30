package com.wxipad.wechat;


import com.wxipad.wechat.tools.crypto.BASE64;
import com.wxipad.wechat.tools.data.DataObj;
import com.wxipad.wechat.tools.listener.ListenerXBase;
import com.wxipad.wechat.tools.listener.ListenerXPool;
import com.wxipad.wechat.tools.tool.ToolBytes;
import com.wxipad.wechat.tools.tool.ToolDate;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class WechatClient {
    public static final int TYPE_IPAD = 1;
    public static final int TYPE_IMAC = 2;
    public static final String NAME_SERVER_NUT = "com.wxipad.wx.WechatServer";
    public static final String NAME_SERVER_GRPC = "com.wxipad.wechat.WechatServer";
    public static final String[] NAMES_SERVER = {NAME_SERVER_NUT, NAME_SERVER_GRPC};
    public static final String NAME_CLIENT = WechatClient.class.getName();
    public static final Listener LISTENER = new Listener();
    public static final AtomicReference<String> current = new AtomicReference<>(null);
    private static final long DEAD_MS = 24 * 60 * 60 * 1000l;
    private static final HashMap<String, Callback> callbacks = new HashMap<>();

    /**
     * @param type
     * @param callback
     * @return
     */
    public static String create(int type, Callback callback) {
        DataObj param = new DataObj();
        param.put("type", type);
        DataObj result = send("create", param);
        if (result.getBoolean("success", false)) {
            String randomId = result.getString("id", null);
            if (randomId != null) {
                synchronized (callbacks) {
                    callbacks.put(randomId, callback);
                }
                return randomId;
            }
        }
        return null;
    }

    /**
     * 创建
     * @param randomId
     * @param callback
     * @return
     */
    public static String create(String randomId, int type, Callback callback) {
        DataObj param = new DataObj();
        param.put("type", type);
        DataObj result = send("create", param);
        if (result.getBoolean("success", false)) {
            if (randomId != null && randomId.isEmpty()) {
                result.put("id", randomId);
            }
            randomId = result.getString("id", randomId);
            if (randomId != null) {
                synchronized (callbacks) {
                    callbacks.put(randomId, callback);
                }
                return randomId;
            }
        }
        return null;
    }
    /**
     * 退出
     * @param randomId
     * @return
     */
    public static boolean exist(String randomId) {
        DataObj result = send("exist", pack(randomId));
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @return
     */
    public static boolean dispose(String randomId) {
        DataObj result = send("dispose", pack(randomId));
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * 初始化
     * @param randomId
     * @param name
     * @param check
     * @param seed
     * @return
     */
    public static boolean init(String randomId, String name, String check, String seed) {
        DataObj param = pack(randomId).put("name", name);
        param.put("check", check).put("seed", seed);
        DataObj result = send("init", param);
        if (result.getBoolean("success", false)) {
            return true;
        }
        return false;
    }

    /**
     * 绑定
     * @param randomId
     * @param name
     * @return
     */
    public static boolean name(String randomId, String name) {
        DataObj param = pack(randomId).put("name", name);
        DataObj result = send("name", param);
        if (result.getBoolean("success", false)) {
            return true;
        }
        return false;
    }

    /**
     * @param randomId
     * @param check
     * @return
     */
    public static boolean check(String randomId, String check) {
        DataObj param = pack(randomId).put("check", check);
        DataObj result = send("check", param);
        if (result.getBoolean("success", false)) {
            return true;
        }
        return false;
    }

    /**
     * 获取登录二维码
     * @param randomId
     * @return
     */
    public static GetLoginQrcode getLoginQrcode(String randomId) {
        DataObj result = send("getLoginQrcode", pack(randomId));
        if (result.getBoolean("success", false)) {
            GetLoginQrcode obj = new GetLoginQrcode();
            obj.qrcode = result.get("qrcode", byte[].class);
            return obj;
        }
        return null;
    }

    /**
     * 检查扫码状态
     * @param randomId
     * @return
     */
    public static CheckLoginQrcode checkLoginQrcode(String randomId) {
        DataObj result = send("checkLoginQrcode", pack(randomId));
        if (result.getBoolean("success", false)) {
            CheckLoginQrcode obj = new CheckLoginQrcode();
            obj.status = result.getInt("status", 0);
            obj.nickName = result.getString("nickName", null);
            obj.headUrl = result.getString("headUrl", null);
            obj.expiry = result.getInt("expiry", 0);
            return obj;
        }
        return null;
    }

    /**
     * 发起登录
     * @param randomId
     * @return
     */
    public static boolean manualAuth(String randomId) {
        DataObj result = send("manualAuth", pack(randomId));
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * 断线重连
     * @param randomId
     * @return
     */
    public static boolean autoAuth(String randomId) {
        DataObj result = send("autoAuth", pack(randomId));
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     *
     * @param randomId
     * @return
     */
    public static AutoLoginPack getAutoLoginPack(String randomId) {
        DataObj result = send("getAutoLoginPack", pack(randomId));
        if (result.getBoolean("success", false)) {
            AutoLoginPack obj = new AutoLoginPack();
            obj.token = result.get("token", byte[].class);
            obj.data = result.get("data", byte[].class);
            return obj;
        }
        return null;
    }

    /**
     * @param randomId
     * @param token
     * @param data
     * @return
     */
    public static boolean setAutoLoginPack(String randomId, String token, String data) {
        DataObj param = pack(randomId);
        param.put("token", token).put("data", data);
        DataObj result = send("setAutoLoginPack", param);
        if (result.getBoolean("success", false)) {
            return true;
        }
        return false;
    }

    /**
     * @param randomId
     * @param pack
     * @return
     */
    public static boolean setAutoLoginPack(String randomId, AutoLoginPack pack) {
        DataObj param = pack(randomId);
        param.put("token", pack.getToken()).put("data", pack.getData());
        DataObj result = send("setAutoLoginPack", param);
        if (result.getBoolean("success", false)) {
            return true;
        }
        return false;
    }

    /**
     * @param randomId
     * @param wxid
     * @param content
     * @return
     */
    public static boolean sendText(String randomId, String wxid, String content) {
        return sendText(randomId, wxid, content, null);
    }

    /**
     * @param randomId
     * @param wxid
     * @param content
     * @param at
     * @return
     */
    public static boolean sendText(String randomId, String wxid, String content, String[] at) {
        DataObj param = pack(randomId);
        param.put("wxid", wxid).put("content", content).put("at", at);
        DataObj result = send("sendText", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param wxid
     * @param path
     * @return
     */
    public static boolean sendImage(String randomId, String wxid, String path) {
        return sendImage(randomId, wxid, path, null);
    }

    /**
     * @param randomId
     * @param wxid
     * @param data
     * @return
     */
    public static boolean sendImage(String randomId, String wxid, byte[] data) {
        return sendImage(randomId, wxid, null, data);
    }

    /**
     * @param randomId
     * @param wxid
     * @param path
     * @param data
     * @return
     */
    private static boolean sendImage(String randomId, String wxid, String path, byte[] data) {
        DataObj param = pack(randomId).put("wxid", wxid);
        param.put("path", path).put("data", data);
        DataObj result = send("sendImage", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param wxid
     * @param path
     * @return
     */
    public static boolean sendCdnImage(String randomId, String wxid, String path) {
        return sendCdnImage(randomId, wxid, path, null, null);
    }

    /**
     * @param randomId
     * @param wxid
     * @param data
     * @return
     */
    public static boolean sendCdnImage(String randomId, String wxid, byte[] data) {
        return sendCdnImage(randomId, wxid, null, data, null);
    }

    /**
     * @param randomId
     * @param wxid
     * @param image
     * @return
     */
    public static boolean sendCdnImage(String randomId, String wxid, BufferedImage image) {
        return sendCdnImage(randomId, wxid, null, null, image);
    }

    /**
     * @param randomId
     * @param wxid
     * @param path
     * @param data
     * @param image
     * @return
     */
    private static boolean sendCdnImage(String randomId, String wxid, String path, byte[] data, BufferedImage image) {
        DataObj param = pack(randomId).put("wxid", wxid);
        param.put("path", path).put("data", data).put("image", image);
        DataObj result = send("sendCdnImage", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param wxid
     * @param content
     * @return
     */
    public static boolean sendRecvImage(String randomId, String wxid, String content) {
        DataObj param = pack(randomId).put("wxid", wxid).put("content", content);
        DataObj result = send("sendRecvImage", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param wxid
     * @param content
     * @return
     */
    public static boolean sendRecvVideo(String randomId, String wxid, String content) {
        DataObj param = pack(randomId).put("wxid", wxid).put("content", content);
        DataObj result = send("sendRecvVideo", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param path
     * @return
     */
    public static SnsUpload snsUpload(String randomId, String path) {
        return snsUpload(randomId, path, null);
    }

    /**
     * @param randomId
     * @param data
     * @return
     */
    public static SnsUpload snsUpload(String randomId, byte[] data) {
        return snsUpload(randomId, null, data);
    }

    /**
     * @param randomId
     * @param path
     * @param data
     * @return
     */
    private static SnsUpload snsUpload(String randomId, String path, byte[] data) {
        DataObj param = pack(randomId);
        param.put("path", path).put("data", data);
        DataObj result = send("snsUpload", param);
        if (result.getBoolean("success", false)) {
            SnsUpload obj = new SnsUpload();
            obj.urlImage = result.getString("urlImage", null);
            obj.urlThumb = result.getString("urlThumb", null);
            obj.size = result.getInt("size", 0);
            return obj;
        }
        return null;
    }

    /**
     * @param randomId
     * @param xml
     * @return
     */
    public static SnsObj snsSendXml(String randomId, String xml) {
        DataObj param = pack(randomId).put("xml", xml);
        DataObj result = send("snsSendXml", param);
        if (result.getBoolean("success", false)) {
            SnsObj obj = new SnsObj();
            obj.id = result.getString("id", null);
            obj.userName = result.getString("userName", null);
            obj.createTime = result.getInt("createTime", 0);
            obj.objectDesc = result.getString("objectDesc", null);
            return obj;
        }
        return null;
    }

    /**
     * @param randomId
     * @param content
     * @return
     */
    public static SnsObj snsSend(String randomId, String content) {
        return snsSend(randomId, content, null);
    }

    /**
     * @param randomId
     * @param content
     * @param paths
     * @return
     */
    public static SnsObj snsSend(String randomId, String content, String[] paths) {
        DataObj param = pack(randomId);
        param.put("content", content).put("paths", paths);
        DataObj result = send("snsSend", param);
        if (result.getBoolean("success", false)) {
            SnsObj obj = new SnsObj();
            obj.id = result.getString("id", null);
            obj.userName = result.getString("userName", null);
            obj.createTime = result.getInt("createTime", 0);
            obj.objectDesc = result.getString("objectDesc", null);
            return obj;
        }
        return null;
    }

    /**
     * @param randomId
     * @param sns
     * @param wxid
     * @param content
     * @return
     */
    public static boolean snsComment(String randomId, String sns, String wxid, String content) {
        DataObj param = pack(randomId);
        param.put("sns", sns).put("wxid", wxid).put("content", content);
        DataObj result = send("snsComment", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param v1
     * @param v2
     * @return
     */
    public static boolean acceptUser(String randomId, String v1, String v2) {
        DataObj param = pack(randomId);
        param.put("v1", v1).put("v2", v2);
        DataObj result = send("acceptUser", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param v1
     * @param v2
     * @param type
     * @param verify
     * @return
     */
    public static boolean addUser(String randomId, String v1, String v2, int type, String verify) {
        DataObj param = pack(randomId);
        param.put("v1", v1).put("v2", v2);
        param.put("type", type).put("verify", verify);
        DataObj result = send("addUser", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * @param randomId
     * @param wxid
     * @return
     */
    public static ContactSearch searchUser(String randomId, String wxid) {
        DataObj param = pack(randomId).put(wxid, wxid);
        DataObj result = send("searchUser", param);
        if (result.getBoolean("success", false)) {
            ContactSearch obj = new ContactSearch();
            obj.userName = result.getString("userName", null);
            obj.nickName = result.getString("nickName", null);
            obj.bigHead = result.getString("bigHead", null);
            obj.smallHead = result.getString("smallHead", null);
            obj.sex = result.getInt("sex", 0);
            obj.signature = result.getString("signature", null);
            obj.country = result.getString("country", null);
            obj.province = result.getString("province", null);
            obj.city = result.getString("city", null);
            obj.v1 = result.getString("v1", null);
            obj.v2 = result.getString("v2", null);
            return obj;
        }
        return null;
    }

    /**
     * @param randomId
     * @param wxid
     * @param style
     * @return
     */
    public static GetQrcode getQrcode(String randomId, String wxid, int style) {
        DataObj param = pack(randomId);
        param.put("wxid", wxid).put("style", style);
        DataObj result = send("getQrcode", param);
        if (result.getBoolean("success", false)) {
            GetQrcode obj = new GetQrcode();
            obj.qrcode = result.get("qrcode", byte[].class);
            return obj;
        }
        return null;
    }

    /**
     * @param randomId
     * @param chatroom
     * @return
     */
    public static ArrayList<GroupMember> getChatRoomMember(String randomId, String chatroom) {
        DataObj param = pack(randomId).put("chatroom", chatroom);
        DataObj result = send("getChatRoomMember", param);
        if (result.getBoolean("success", false)) {
            ArrayList<GroupMember> list = new ArrayList<>();
            DataObj[] members = result.get("members", DataObj[].class);
            for (DataObj member : members) {
                GroupMember obj = new GroupMember();
                obj.userName = member.getString("userName", null);
                obj.nickName = member.getString("nickName", null);
                obj.displayName = member.getString("displayName", null);
                obj.bigHeadImgUrl = member.getString("bigHeadImgUrl", null);
                obj.smallHeadImgUrl = member.getString("smallHeadImgUrl", null);
                obj.inviteUser = member.getString("inviteUser", null);
                list.add(obj);
            }
            return list;
        }
        return null;
    }

    /**
     * @param randomId
     * @param chatroom
     * @param wxid
     * @return
     */
    public static boolean deleteChatRoomMember(String randomId, String chatroom, String wxid) {
        DataObj param = pack(randomId);
        param.put("chatroom", chatroom).put("wxid", wxid);
        DataObj result = send("deleteChatRoomMember", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * 设置群公告
     *
     * @param randomId
     * @param chatroom
     * @param announcement
     * @return
     */
    public static boolean setChatRoomAnnouncement(String randomId, String chatroom, String announcement) {
        DataObj param = pack(randomId);
        param.put("chatroom", chatroom).put("announcement", announcement);
        DataObj result = send("setChatRoomAnnouncement", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    public static String self(String randomId) {
        DataObj result = send("self", pack(randomId));
        if (result.getBoolean("success", false)) {
            return result.getString("result", null);
        }
        return null;
    }

    /**
     * @param randomId
     * @return
     */
    public static boolean online(String randomId) {
        return online(randomId, 0l);
    }

    /**
     * @param randomId
     * @param wait
     * @return
     */
    public static boolean online(String randomId, long wait) {
        DataObj param = pack(randomId).put("wait", wait);
        DataObj result = send("online", param);
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * 退出
     *
     * @param randomId
     * @return
     */
    public static boolean logout(String randomId) {
        DataObj result = send("logout", pack(randomId));
        if (result.getBoolean("success", false)) {
            return result.getBoolean("result", false);
        }
        return false;
    }

    /**
     * 组包
     *
     * @param randomId
     * @return
     */
    private static DataObj pack(String randomId) {
        return new DataObj().put("id", randomId);
    }

    /**
     * 发送
     *
     * @param cmd
     * @param param
     * @return
     */
    private static DataObj send(String cmd, DataObj param) {
        String name = current.get();
        if (name != null) {
            DataObj[] results = ListenerXPool.call(NAME_CLIENT, name, cmd, param);
            if (results != null) {
                for (DataObj result : results) {
                    if (result.has("success")) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static boolean free(String randomId) {
        synchronized (callbacks) {
            return callbacks.remove(randomId) != null;
        }
    }

    public static int free() {
        int count = 0;
        synchronized (callbacks) {
            ArrayList<String> keys = new ArrayList<String>();
            keys.addAll(callbacks.keySet());
            for (String key : keys) {
                Callback callback = callbacks.get(key);
                if (callback.dead()) {
                    callbacks.remove(key);
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean startup(ClassLoader cl, Object... params) {
        String name = test(cl);
        if (name != null) {
            current.set(name);
            ListenerXPool.remove(LISTENER.getName());//确保无重复注册
            ListenerXPool.add(LISTENER);//注册监听器
            return invoke(cl, name, "startup", params);//启动微信服务器
        }
        return false;
    }

    public static boolean shutdown(ClassLoader cl) {
        String name = current.getAndSet(null);
        try {
            if (name != null) {
                ListenerXPool.remove(LISTENER.getName());//移除监听器
                return invoke(cl, name, "shutdown");//关闭微信服务器
            } else {
                return false;
            }
        } finally {
            current.set(null);
        }
    }

    private static boolean invoke(ClassLoader cl, String clsName, String methodName, Object... args) {
        try {
            ClassLoader classLoader = cl;
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            Class cls = classLoader.loadClass(clsName);
            Class[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
            Object obj = cls.getMethod(methodName, types).invoke(null, args);
            return Boolean.TRUE.equals(obj);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String test(ClassLoader cl) {
        ClassLoader classLoader = cl;
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        for (String name : NAMES_SERVER) {
            try {
                classLoader.loadClass(name);
                return name;
            } catch (ClassNotFoundException e) {
            }
        }
        return null;
    }

    public static class Callback {

        public final AtomicLong active = new AtomicLong(ToolDate.now());

        public boolean dead() {
            return active.get() + DEAD_MS < ToolDate.now();
        }

        public void online(String randomId) {
        }

        public void offline(String randomId) {
        }

        public void privateText(String randomId, String from_user, String to_user, long timestamp, String content, boolean self) {
        }

        public void groupText(String randomId, String from_group, String from_user, String to_user, long timestamp, String content, String[] atlist, boolean self) {
        }

        public void privateImage(String randomId, String from_user, String to_user, long timestamp, String content, byte[] thumb, boolean self) {
        }

        public void groupImage(String randomId, String from_group, String from_user, String to_user, long timestamp, String content, byte[] thumb, boolean self) {
        }

        public void privateVideo(String randomId, String from_user, String to_user, long timestamp, String content, boolean self) {
        }

        public void groupVideo(String randomId, String from_group, String from_user, String to_user, long timestamp, String content, boolean self) {
        }

        public void groupInvite(String randomId, String from_group, String from_user, String to_user, long timestamp, String nick_name) {
        }

        public void friendAdd(String randomId, String from_user, String v1, String v2, long timestamp, String remark, String source_user) {
        }

    }

    public static class Listener extends ListenerXBase {

        public Listener() {
            super(NAME_CLIENT);
        }

        @Override
        protected boolean accept(String fromName, String toName, String cmd) {
            for (String name : NAMES_SERVER) {
                if (name.equals(fromName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected DataObj receive(String fromName, String toName, String cmd, DataObj param) {
            if (param != null && param.has("id")) {
                String randomId = param.getString("id", null);
                Callback callback = null;
                if (randomId != null) {
                    synchronized (callbacks) {
                        callback = callbacks.get(randomId);
                    }
                }
                if (callback != null) {
                    callback.active.set(ToolDate.now());//更新回调活跃时间
                    if ("online".equals(cmd)) {
                        callback.online(randomId);
                    } else if ("offline".equals(cmd)) {
                        callback.offline(randomId);
                    } else if ("privateText".equals(cmd)) {
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String content = param.getString("content", null);
                        boolean self = param.getBoolean("self", false);
                        callback.privateText(randomId, from_user, to_user, timestamp, content, self);
                    } else if ("groupText".equals(cmd)) {
                        String from_group = param.getString("from_group", null);
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String content = param.getString("content", null);
                        String[] atlist = param.get("atlist", String[].class);
                        boolean self = param.getBoolean("self", false);
                        callback.groupText(randomId, from_group, from_user, to_user, timestamp, content, atlist, self);
                    } else if ("privateImage".equals(cmd)) {
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String content = param.getString("content", null);
                        byte[] thumb = param.get("thumb", byte[].class);
                        boolean self = param.getBoolean("self", false);
                        callback.privateImage(randomId, from_user, to_user, timestamp, content, thumb, self);
                    } else if ("groupImage".equals(cmd)) {
                        String from_group = param.getString("from_group", null);
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String content = param.getString("content", null);
                        byte[] thumb = param.get("thumb", byte[].class);
                        boolean self = param.getBoolean("self", false);
                        callback.groupImage(randomId, from_group, from_user, to_user, timestamp, content, thumb, self);
                    } else if ("privateVideo".equals(cmd)) {
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String content = param.getString("content", null);
                        boolean self = param.getBoolean("self", false);
                        callback.privateVideo(randomId, from_user, to_user, timestamp, content, self);
                    } else if ("groupVideo".equals(cmd)) {
                        String from_group = param.getString("from_group", null);
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String content = param.getString("content", null);
                        boolean self = param.getBoolean("self", false);
                        callback.groupVideo(randomId, from_group, from_user, to_user, timestamp, content, self);
                    } else if ("groupInvite".equals(cmd)) {
                        String from_group = param.getString("from_group", null);
                        String from_user = param.getString("from_user", null);
                        String to_user = param.getString("to_user", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String nick_name = param.getString("nick_name", null);
                        callback.groupInvite(randomId, from_group, from_user, to_user, timestamp, nick_name);
                    } else if ("friendAdd".equals(cmd)) {
                        String from_user = param.getString("from_user", null);
                        String v1 = param.getString("v1", null);
                        String v2 = param.getString("v2", null);
                        long timestamp = param.getLong("timestamp", 0l);
                        String remark = param.getString("remark", null);
                        String source_user = param.getString("source_user", null);
                        callback.friendAdd(randomId, from_user, v1, v2, timestamp, remark, source_user);
                    }
                }
            }
            return null;
        }
    }

    public static class GetLoginQrcode {
        public byte[] qrcode;//二维码图片数据
    }

    public static class CheckLoginQrcode {
        public int status;//二维码状态，0-未扫码，1-已扫码，2-已确认，4-已取消
        public String nickName;//用户昵称
        public String headUrl;//用户头像
        public int expiry;//二维码有效期
    }

    /**
     *
     */
    public static class SnsUpload {
        public String urlImage;//大图片URL地址
        public String urlThumb;//小图片URL地址
        public int size;//上传图片大小
    }

    /**
     *
     */
    public static class SnsObj {
        public String id;//朋友圈ID
        public String userName;//相关用户wxid
        public int createTime;//朋友圈发布时间
        public String objectDesc;//朋友圈内容
    }

    /**
     *
     */
    public static class GetQrcode {
        public byte[] qrcode;//二维码图片数据
    }

    /**
     *
     */
    public static class ContactSearch {
        public String userName;//微信ID
        public String nickName;//成员昵称
        public String bigHead;//微信大头像
        public String smallHead;//微信小头像
        public int sex;//性别
        public String signature;//签名档
        public String country;//国
        public String province;//省
        public String city;//市
        public String v1;//添加好友v1信息
        public String v2;//添加好友v2信息
    }

    /**
     *
     */
    public static class GroupMember {
        public String userName;
        public String nickName;
        public String displayName;
        public String bigHeadImgUrl;
        public String smallHeadImgUrl;
        public String inviteUser;
    }

    /**
     *
     */
    public static class AutoLoginPack {

        public byte[] token;
        public byte[] data;

        public static AutoLoginPack create(String token, String data) {
            AutoLoginPack pack = new AutoLoginPack();
            pack.token = ToolBytes.hex2Bytes(token);
            pack.data = BASE64.decode(data);
            return pack;
        }

        public String getToken() {
            return ToolBytes.bytes2Hex(token);
        }

        public String getData() {
            return BASE64.encode(data);
        }

    }

}
