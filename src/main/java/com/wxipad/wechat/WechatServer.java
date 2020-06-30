package com.wxipad.wechat;


import com.google.protobuf.InvalidProtocolBufferException;
import com.wxipad.wechat.tools.data.DataObj;
import com.wxipad.wechat.tools.listener.ListenerXBase;
import com.wxipad.wechat.tools.listener.ListenerXPool;
import com.wxipad.wechat.tools.model.WechatApiMsg;

import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class WechatServer {

    public static final AtomicBoolean running = new AtomicBoolean(false);
    public static final String NAME = WechatServer.class.getName();
    public static final HookListener LISTENER = new HookListener();
    public static final WechatApi.Callback CALLBACK = new WechatApi.Callback() {

        @Override
        public void online(WechatIns ins) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            broadcast("online", param);
        }

        @Override
        public void offline(WechatIns ins) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            broadcast("offline", param);
        }

        @Override
        public void privateText(WechatIns ins, String from_user, String to_user, long timestamp, String content, boolean self) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("content", content);
            param.put("self", self);
            broadcast("privateText", param);
        }

        @Override
        public void groupText(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, String[] atlist, boolean self) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_group", from_group);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("content", content);
            param.put("atlist", atlist);
            param.put("self", self);
            broadcast("groupText", param);
        }

        @Override
        public void privateImage(WechatIns ins, String from_user, String to_user, long timestamp, String content, byte[] thumb, boolean self) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("content", content);
            param.put("thumb", thumb);
            param.put("self", self);
            broadcast("privateImage", param);
        }

        @Override
        public void groupImage(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, byte[] thumb, boolean self) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_group", from_group);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("content", content);
            param.put("thumb", thumb);
            param.put("self", self);
            broadcast("groupImage", param);
        }

        @Override
        public void privateVideo(WechatIns ins, String from_user, String to_user, long timestamp, String content, boolean self) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("content", content);
            param.put("self", self);
            broadcast("privateVideo", param);
        }

        @Override
        public void groupVideo(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, boolean self) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_group", from_group);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("content", content);
            param.put("self", self);
            broadcast("groupVideo", param);
        }

        @Override
        public void groupInvite(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String nick_name) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_group", from_group);
            param.put("from_user", from_user);
            param.put("to_user", to_user);
            param.put("timestamp", timestamp);
            param.put("nick_name", nick_name);
            broadcast("groupInvite", param);
        }

        @Override
        public void friendAdd(WechatIns ins, String from_user, String v1, String v2, long timestamp, String remark, String source_user) {
            DataObj param = new DataObj();
            param.put("id", ins.id);
            param.put("from_user", from_user);
            param.put("v1", v1);
            param.put("v2", v2);
            param.put("timestamp", timestamp);
            param.put("remark", remark);
            param.put("source_user", source_user);
            broadcast("friendAdd", param);
        }

        private void broadcast(String cmd, DataObj param) {
            if (running.get()) {
                ListenerXPool.call(NAME, null, cmd, param);
            }
        }

    };

    public static boolean startup(String config) {
        if (running.compareAndSet(false, true)) {
            if (config != null) {
                WechatObj.GrpcConfig obj = WechatTool.gsonObj(config, WechatObj.GrpcConfig.class);
                if (obj != null) {
                    WechatObj.GrpcConfig.init(obj);
                }
            }
            ListenerXPool.add(LISTENER);
            return true;
        }
        return false;
    }

    public static boolean shutdown() {
        if (running.compareAndSet(true, false)) {
            WechatApi.dispose();
            WechatObj.GrpcConfig.dispose();
            ListenerXPool.remove(LISTENER);
            return true;
        }
        return false;
    }

    public static class HookListener extends ListenerXBase {

        public HookListener() {
            super(NAME);
        }

        @Override
        protected boolean accept(String fromName, String toName, String cmd) {
            return running.get() ? NAME.equals(toName) : false;
        }

        @Override
        protected DataObj receive(String fromName, String toName, String cmd, DataObj param) {
            DataObj result = new DataObj();
            WechatApiMsg wechatApiMsg = null;
            try {
                if ("debug".equals(cmd)) {
                    WechatConst.DEBUG = param.getBoolean("value", false);
                    result.put("success", true);
                } else if ("create".equals(cmd)) {
                    int type = param.getInt("type", 0);
                    String id = null;
                    try {
                        id = WechatApi.create(type, CALLBACK, wechatApiMsg).randomId;
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    result.put("id", id);
                } else if ("exist".equals(cmd)) {
                    String id = param.getString("id", null);
                    result.put("result", WechatApi.exist(id));
                } else if ("dispose".equals(cmd)) {
                    String id = param.getString("id", null);
                    result.put("result", WechatApi.dispose(id));
                } else if ("init".equals(cmd)) {
                    String id = param.getString("id", null);
                    String name = param.getString("name", null);
                    String seed = param.getString("seed", null);
                    WechatApi.init(id, name, seed);
                } else if ("deviceName".equals(cmd)) {
                    String id = param.getString("id", null);
                    String deviceName = param.getString("deviceName", null);
                    WechatApi.deviceName(id, deviceName);
                } else if ("getLoginQrcode".equals(cmd)) {
                    String id = param.getString("id", null);
                    WechatObj.LoginQrcode obj = WechatApi.getLoginQrcode(id);
                    byte[] data = Base64.getDecoder().decode(obj.ImgBuf);
                    if (obj != null) {
                        result.put("qrcode", data);
                        result.put("qrcodeUrl", "http://weixin.qq.com/x/" + obj.Uuid);
                    }
                } else if ("checkLoginQrcode".equals(cmd)) {
                    String id = param.getString("id", null);
                    WechatObj.LoginQrcode obj = WechatApi.checkLoginQrcode(id);
                    if (obj != null) {
                        result.put("status", obj.Status);
                        result.put("nickName", obj.Nickname);
                        result.put("headUrl", obj.HeadImgUrl);
                        result.put("expiry", obj.ExpiredTime);
                    }
                } else if ("manualAuth".equals(cmd)) {
                    String id = param.getString("id", null);
                    result.put("result", WechatApi.manualAuth(1111, id));
                } else if ("autoAuth".equals(cmd)) {
                    String id = param.getString("id", null);
                    result.put("result", WechatApi.autoAuth(id));
                } else if ("getAutoLoginPack".equals(cmd)) {
                    String id = param.getString("id", null);
                    WechatApi.AutoLoginPack obj = WechatApi.getAutoLoginPack(id);
                    if (obj != null) {
                        result.put("token", obj.token);
                        result.put("data", obj.data);
                    }
                } else if ("setAutoLoginPack".equals(cmd)) {
                    String id = param.getString("id", null);
                    String token = param.getString("token", null);
                    String data = param.getString("data", null);
                    WechatApi.setAutoLoginPack(id, token, data);
                } else if ("sendText".equals(cmd)) {
                    String id = param.getString("id", null);
                    String wxid = param.getString("wxid", null);
                    String content = param.getString("content", null);
                    String[] at = param.get("at", String[].class);
                    result.put("result", WechatApi.sendText(id, wxid, content, at));
                } else if ("sendImage".equals(cmd) || "sendCdnImage".equals(cmd)) {
                    String id = param.getString("id", null);
                    String wxid = param.getString("wxid", null);
                    String path = param.getString("path", null);
                    byte[] data = param.get("data", byte[].class);
                    if (path != null) {
                        result.put("result", WechatApi.sendImage(id, wxid, path));
                    } else if (data != null) {
                        result.put("result", WechatApi.sendImage(id, wxid, data));
                    } else {
                        result.put("result", false);
                    }
                } else if ("sendRecvImage".equals(cmd)) {
                    String id = param.getString("id", null);
                    String wxid = param.getString("wxid", null);
                    String content = param.getString("content", null);
                    result.put("result", WechatApi.sendRecvImage(id, wxid, content));
                } else if ("sendRecvVideo".equals(cmd)) {
                    String id = param.getString("id", null);
                    String wxid = param.getString("wxid", null);
                    String content = param.getString("content", null);
                    result.put("result", WechatApi.sendRecvVideo(id, wxid, content));
                } else if ("snsUpload".equals(cmd)) {
                    String id = param.getString("id", null);
                    String path = param.getString("path", null);
                    byte[] data = param.get("data", byte[].class);
                    WechatApi.SnsUpload obj = null;
                    if (path != null) {
                        obj = WechatApi.snsUpload(id, path);
                    } else if (data != null) {
                        obj = WechatApi.snsUpload(id, data);
                    }
                    if (obj != null) {
                        result.put("urlImage", obj.urlImage);
                        result.put("urlThumb", obj.urlThumb);
                        result.put("size", obj.size);
                    }
                } else if ("snsSendXml".equals(cmd)) {
                    String id = param.getString("id", null);
                    String xml = param.getString("xml", null);
                    WechatApi.SnsObj obj = WechatApi.snsSendXml(id, xml);
                    if (obj != null) {
                        result.put("id", obj.id);
                        result.put("userName", obj.userName);
                        result.put("createTime", obj.createTime);
                        result.put("objectDesc", obj.objectDesc);
                    }
                } else if ("snsSend".equals(cmd)) {
                    String id = param.getString("id", null);
                    String content = param.getString("content", null);
                    String[] paths = param.get("paths", String[].class);
                    WechatApi.SnsObj obj = null;
                    if (paths == null) {
                        obj = WechatApi.snsSend(id, content);
                    } else {
                        obj = WechatApi.snsSend(id, content, paths);
                    }
                    if (obj != null) {
                        result.put("id", obj.id);
                        result.put("userName", obj.userName);
                        result.put("createTime", obj.createTime);
                        result.put("objectDesc", obj.objectDesc);
                    }
                } else if ("snsComment".equals(cmd)) {
                    String id = param.getString("id", null);
                    String sns = param.getString("sns", null);
                    String wxid = param.getString("wxid", null);
                    String content = param.getString("content", null);
                    result.put("result", WechatApi.snsComment(id, sns, wxid, content));
                } else if ("acceptUser".equals(cmd)) {
                    String id = param.getString("id", null);
                    String v1 = param.getString("v1", null);
                    String v2 = param.getString("v2", null);
                    result.put("result", WechatApi.acceptUser(id, v1, v2));
                } else if ("addUser".equals(cmd)) {
                    String id = param.getString("id", null);
                    String v1 = param.getString("v1", null);
                    String v2 = param.getString("v2", null);
                    int type = param.getInt("type", 0);
                    String verify = param.getString("verify", null);
                    result.put("result", WechatApi.addUser(id, v1, v2, type, verify));
                } else if ("searchUser".equals(cmd)) {
                    String id = param.getString("id", null);
                    String wxid = param.getString("wxid", null);
                    WechatApi.ContactSearch obj = WechatApi.searchUser(id, wxid);
                    if (obj != null) {
                        result.put("userName", obj.userName);
                        result.put("nickName", obj.nickName);
                        result.put("bigHead", obj.bigHead);
                        result.put("smallHead", obj.smallHead);
                        result.put("sex", obj.sex);
                        result.put("signature", obj.signature);
                        result.put("country", obj.country);
                        result.put("province", obj.province);
                        result.put("city", obj.city);
                        result.put("v1", obj.v1);
                        result.put("v2", obj.v2);
                    }
                } else if ("getQrcode".equals(cmd)) {
                    String id = param.getString("id", null);
                    String wxid = param.getString("wxid", null);
                    int style = param.getInt("style", 0);
                    WechatApi.GetQrcode obj = WechatApi.getQrcode(id, wxid, style);
                    if (obj != null) {
                        result.put("qrcode", obj.qrcode);
                    }
                } else if ("getChatRoomMember".equals(cmd)) {
                    String id = param.getString("id", null);
                    String chatroom = param.getString("chatroom", null);
                    ArrayList<WechatApi.GroupMember> objs = WechatApi.getChatRoomMember(id, chatroom);
                    if (objs != null) {
                        DataObj[] members = new DataObj[objs.size()];
                        for (int i = 0; i < objs.size(); i++) {
                            DataObj member = new DataObj();
                            WechatApi.GroupMember obj = objs.get(i);
                            member.put("userName", obj.userName);
                            member.put("nickName", obj.nickName);
                            member.put("displayName", obj.displayName);
                            member.put("bigHeadImgUrl", obj.bigHeadImgUrl);
                            member.put("smallHeadImgUrl", obj.smallHeadImgUrl);
                            member.put("inviteUser", obj.inviteUser);
                            members[i] = member;
                        }
                        result.put("members", members);
                    }
                } else if ("deleteChatRoomMember".equals(cmd)) {
                    String id = param.getString("id", null);
                    String chatroom = param.getString("chatroom", null);
                    String wxid = param.getString("wxid", null);
                    result.put("result", WechatApi.deleteChatRoomMember(id, chatroom, wxid));
                } else if ("setChatRoomAnnouncement".equals(cmd)) {
                    String id = param.getString("id", null);
                    String chatroom = param.getString("chatroom", null);
                    String announcement = param.getString("announcement", null);
                    result.put("result", WechatApi.setChatRoomAnnouncement(id, chatroom, announcement));
                } else if ("self".equals(cmd)) {
                    String id = param.getString("id", null);
                    result.put("result", WechatApi.self(id));
                } else if ("online".equals(cmd)) {
                    String id = param.getString("id", null);
                    long wait = param.getLong("wait", 0l);
                    if (wait <= 0) {
                        result.put("result", WechatApi.online(id));
                    } else {
                        result.put("result", WechatApi.online(id, wait));
                    }
                } else if ("logout".equals(cmd)) {
                    String id = param.getString("id", null);
                    result.put("result", WechatApi.logout(id));
                }
                result.put("success", true);
            } catch (WechatApi.ApiException e) {
                result.put("success", false);
                result.put("error", e.getMessage());
            }
            return result;
        }
    }


}
