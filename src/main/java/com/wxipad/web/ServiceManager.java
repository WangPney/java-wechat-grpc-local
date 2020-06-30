package com.wxipad.web;


import com.google.protobuf.InvalidProtocolBufferException;
import com.wxipad.wechat.ServeiceDemo;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceManager {
    private static final ConcurrentHashMap<String, ServeiceDemo> serviceMap = new ConcurrentHashMap<String, ServeiceDemo>(10000, 0.90f, Runtime.getRuntime().availableProcessors() * 2);
    private static final ScheduledExecutorService SERVICE_MONITOR = new ScheduledThreadPoolExecutor(5, new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
    private static final ServiceManager INSTANCE = new ServiceManager();


    static {
        SERVICE_MONITOR.scheduleAtFixedRate(() -> {
            int lixian = 0;
            try {
                for (Iterator<Map.Entry<String, ServeiceDemo>> it = serviceMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, ServeiceDemo> item = it.next();
                    ServeiceDemo service = item.getValue();
                    if (service.isDead()) {
                        lixian++;
                        service.setOffline(true, lixian);
                        service.exit();
                        it.remove();

                    }
                }
            } catch (Exception e) {
                log.info("" + e);
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    public static ServiceManager getInstance() {
        return INSTANCE;
    }

    public static void uploginedUsers() {
        try {
            log.info("读取redis，加载已经登录的用户···");
            Map<byte[], byte[]> loginedUsers = RedisUtils.hGetAll(ApplicationRunnerImpl.redisId);
            if (loginedUsers != null) {
                Set<byte[]> keySet = loginedUsers.keySet();
                int renshi = 0;
                for (byte[] key : keySet) {
                    renshi++;
                    WechatApiMsg wechatApiMsg = WechatApiMsg.unserizlize(loginedUsers.get(key));
                    SERVICE_MONITOR.submit(() -> {
                        ServeiceDemo service = ServiceManager.getInstance().createServiceForReLogin(wechatApiMsg);
                        // service.loadLoginedUser(wechatApiMsg);
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ServeiceDemo createServiceForReLogin(WechatApiMsg wechatApiMsg) {
        ServeiceDemo service = getServiceByRandomId(wechatApiMsg);
        if (service == null) {
            service = newGrpcService(wechatApiMsg);
        }
        return service;
    }

    public ServeiceDemo newGrpcService(WechatApiMsg wechatApiMsg) {
        ServeiceDemo service;
        if (wechatApiMsg.getSoftwareId().equals("666")) {
            try {
                service = new ServeiceDemo(wechatApiMsg);
                serviceMap.put(wechatApiMsg.getRandomId(), service);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
        return service;
    }

    public ServeiceDemo getServiceByRandomId(WechatApiMsg wechatApiMsg) {
        ServeiceDemo server = serviceMap.get(wechatApiMsg.randomId);
        if (server != null && wechatApiMsg.callbackAddRes != null && !wechatApiMsg.callbackAddRes.equals("")) {
        }
        return server;
    }

}
