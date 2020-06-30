package com.wxipad.web;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WebSocketMapUtil {
    public static ConcurrentMap<String, MyWebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    public static void put(String key, MyWebSocketServer myWebSocketServer) {
        webSocketMap.put(key, myWebSocketServer);
    }
    public static MyWebSocketServer get(String key) {
        return webSocketMap.get(key);
    }
    public static void remove(String key) {
        webSocketMap.remove(key);
    }
    public static Collection<MyWebSocketServer> getValues() {
        return webSocketMap.values();
    }
}