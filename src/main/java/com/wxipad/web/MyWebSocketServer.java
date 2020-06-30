package com.wxipad.web;

import com.alibaba.fastjson.JSON;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.wxipad.web.CommonApi.execute;

@Slf4j
@Component
@ServerEndpoint(value = "/websocket")
public class MyWebSocketServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<MyWebSocketServer> webSocketSet = new CopyOnWriteArraySet<MyWebSocketServer>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    public MyWebSocketServer() {
        log.info("[WebSocketServer] 准备就绪!");
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocketServer.onlineCount--;
    }

    public static boolean sendMessage(String account, String message) throws IOException {
        MyWebSocketServer myWebSocket = getMyWebSocketServer(account);
        if (myWebSocket != null) {
            try {
                myWebSocket.sendMessage(message);
                log.info("主动给用户:[{}]的消息,发送成功!", account);
                return true;
            } catch (IOException e) {
                log.error("给用户:[{}]的消息,发送失败!", account);
                e.printStackTrace();
                return false;
            }
        } else {
            log.error("用户:[{}]已断开连接!发送失败!消息内容：{}", account,message);
            return false;
        }
    }

    public static MyWebSocketServer getMyWebSocketServer(String account) throws IOException {
        return WebSocketMapUtil.get(account);
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();//在线数加1
        String query = session.getQueryString();
        log.info("新用户:[{}]加入! 当前在线人数为:[{}]", query, getOnlineCount());
        WebSocketMapUtil.put(query, this);
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        String query = session.getQueryString();
        WebSocketMapUtil.remove(query);
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();//在线数减1
        log.info("用户:[{}]离开！当前在线人数为:[{}]", query, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        //获取服务端到客户端的通道
        //WechatApiMsg wechatApiMsg = GsonUtil.GsonToBean(params,WechatApiMsg.class);
        WechatApiMsg wechatApiMsg = JSON.parseObject(message, WechatApiMsg.class);
        int cmd = wechatApiMsg.cmd;
        String account = wechatApiMsg.account;
        String query = session.getQueryString();
        String result;
        if (!account.equals(query)) {
            result = "用户数据不匹配，请仔细核对后重试！";
            sendMessage(account, result);
            return;
        } else {
            log.info("收到来自:[" + query + "]的请求:[" + message + "]");
            result = GsonUtil.GsonString(execute(wechatApiMsg));
        }
        if (result.isEmpty()) {
            sendMessage(account, message);
        } else {
            sendMessage(account, result);
        }
    }



    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info(session.getQueryString() + "连接发生错误" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }


}
