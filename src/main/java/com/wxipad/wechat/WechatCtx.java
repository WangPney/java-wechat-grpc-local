package com.wxipad.wechat;

import com.google.protobuf.ByteString;
import com.wxipad.proto.User;

public abstract class WechatCtx {

    /**
     * 获取当前实例的User对象
     *
     * @return 当前实例的User对象
     */
    public abstract User getUser();

    public abstract void sendMsg(String account, String data);
    /**
     * 是否包含CheckClient数据
     *
     * @return 是或者否
     */
    public abstract boolean hasClientCheckDat();

    /**
     * 获取微信版本数字
     *
     * @return 微信版本数字
     */
    public abstract int genClientVersion();


    /**
     * 生成设备类型
     *
     * @return 设备类型
     */
    public abstract String genDeviceType();


    public long getUin() {
        User user = getUser();
        if (user != null) {
            return user.getUin();
        }
        return 0l;
    }

    public String getUsername() {
        User user = getUser();
        if (user != null) {
            String username = user.getUserame();
            if (username != null && !username.isEmpty()) {
                return username;
            }
        }
        return null;
    }

    public String getNickname() {
        User user = getUser();
        if (user != null) {
            String nickname = user.getNickname().toStringUtf8();
            if (nickname != null && !nickname.isEmpty()) {
                return nickname;
            }
        }
        return null;
    }

    public byte[] getSessionKey() {
        User user = getUser();
        if (user != null) {
            ByteString sessionKey = user.getSessionKey();
            if (sessionKey != null) {
                return sessionKey.toByteArray();
            }
        }
        return null;
    }

    public byte[] getSyncKey() {
        User user = getUser();
        if (user != null) {
            ByteString currentsyncKey = user.getCurrentsyncKey();
            if (currentsyncKey != null) {
                return currentsyncKey.toByteArray();
            }
        }
        return null;
    }
    public byte[] getCookies() {
        User user = getUser();
        if (user != null) {
            ByteString cookies = user.getCookies();
            if (cookies != null) {
                return cookies.toByteArray();
            }
        }
        return null;
    }

    public byte[] getDeviceIdBytes() {
        User user = getUser();
        if (user != null) {
            ByteString deviceIdBytes = user.getDeviceIdBytes();
            if (deviceIdBytes != null) {
                return deviceIdBytes.toByteArray();
            }
        }
        return null;
    }

}
