package com.wxipad.wechat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述
 *
 * @author: aweie
 * @date: 2019/7/5 00051:20
 */
public class ServeiceDemo extends WechatExample {
    private static final Logger logger = LoggerFactory.getLogger(ServeiceDemo.class);

    public ServeiceDemo(WechatApiMsg wechatApiMsg) throws InvalidProtocolBufferException {
        super(wechatApiMsg);
    }

    @Override
    public void setOffline(boolean dead, int ss) {

    }

    public boolean isDead() {
        return isDead();
    }
}
