package com.wxipad.client;

import com.google.common.base.Strings;
import com.wxipad.proto.WechatGrpc;
import com.wxipad.proto.WechatMsg;
import com.wxipad.wechat.WechatDebug;
import com.wxipad.wechat.WechatTool;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;

import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.LinkedList;

import static com.wxipad.wechat.WechatTool.getMetaData;

public class GrpcClient {
    public static io.netty.handler.ssl.SslContextBuilder sslContextBuilder;
    private static final HashMap<String, GrpcClient> pool = new HashMap<>();//多组连接池管理
    public final String ip;
    public final int port;
    public final String id;
    public final String key;
    public final String token;
    public final String cert;
    public final Metadata meta;
    private final LinkedList<ManagedChannel> allChannels;//全部连接池
    private final LinkedList<ManagedChannel> freeChannels;//可用连接池

    /**
     * 构造GRPC连接池
     *
     * @param config 连接池配置
     */
    public GrpcClient(Config config) {
        this(config.ip, config.port, config.id, config.key, config.token, config.cert);
    }

    /**
     * 构造GRPC连接池
     *
     * @param ip    服务器IP
     * @param port  服务器端口
     * @param id    身份ID
     * @param key   身份KEY
     * @param token 身份TOKEN
     * @param cert  证书路径
     */
    public GrpcClient(String ip, int port, String id, String key, String token, String cert) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.key = key;
        this.token = token;
        this.cert = cert;
        this.meta = getMetaData(id, key);
        this.allChannels = new LinkedList<>();
        this.freeChannels = new LinkedList<>();
    }



    /**
     * 初始化Grpc连接池
     *
     * @param name   连接池名称
     * @param config 连接池配置
     */
    public static void init(String name, Config config) {
        name = Strings.nullToEmpty(name);
        synchronized (pool) {
            if (pool.containsKey(name)) {
                pool.remove(name).dispose();
            }
            pool.put(name, new GrpcClient(config));
        }
    }

    /**
     * 初始化Grpc连接池
     *
     * @param name  连接池名称
     * @param ip    服务器IP
     * @param port  服务器端口
     * @param id    身份ID
     * @param key   身份KEY
     * @param token 身份TOKEN
     * @param cert  证书路径
     */
    public static void init(String name, String ip, int port, String id, String key, String token, String cert) {
        name = Strings.nullToEmpty(name);
        synchronized (pool) {
            if (pool.containsKey(name)) {
                pool.remove(name).dispose();
            }
            pool.put(name, new GrpcClient(ip, port, id, key, token, cert));
        }
    }

    /**
     * 释放Grpc连接池
     *
     * @param name 连接池名称
     */
    public static void dispose(String name) {
        name = Strings.nullToEmpty(name);
        synchronized (pool) {
            if (pool.containsKey(name)) {
                pool.remove(name).dispose();
            }
        }
    }

    /**
     * 获取Grpc连接池
     *
     * @param name 连接池名称
     * @return Grpc连接池
     */
    public static GrpcClient get(String name) {
        name = Strings.nullToEmpty(name);
        synchronized (pool) {
            return pool.get(name);
        }
    }

    public static GrpcClient get() {
        return get("app");
    }
    /**
     * 创建新连接
     *
     * @return 新的连接会话
     */
    private ManagedChannel newChannel() {
        if (cert != null) {
            try {
                return NettyChannelBuilder.forAddress(ip, port).sslContext(GrpcSslContexts.forClient().trustManager(io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE).build()).build();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
        return NettyChannelBuilder.forAddress(ip, port)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
    }

    /**
     * 借取可用连接
     *
     * @return 连接会话
     */
    private ManagedChannel borrowChannel() {
        ManagedChannel channel, newChannel = null;
        synchronized (this) {
            if (!freeChannels.isEmpty()) {
                //使用老链接
                channel = freeChannels.removeFirst();
            } else {
                //创建新链接
                channel = newChannel = newChannel();
            }
            if (newChannel != null) {
                //添加新链接
                allChannels.addLast(newChannel);
            }
        }
        return channel;
    }

    /**
     * 退还指定连接
     *
     * @param channel 连接会话
     * @return 是否操作成功
     */
    private boolean returnChannel(ManagedChannel channel) {
        synchronized (this) {
            if (allChannels.contains(channel)) {
                if (!freeChannels.contains(channel)) {
                    freeChannels.addLast(channel);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 释放指定连接
     *
     * @param channel 连接会话
     */
    private void disposeChannel(ManagedChannel channel) {
        synchronized (this) {
            allChannels.remove(channel);
            freeChannels.remove(channel);
            if (!channel.isShutdown()) {
                channel.shutdown();
            }
        }
    }

    /**
     * 获取GRPC连接数量
     *
     * @return 当前GRPC连接数量
     */
    public int sizeChannel() {
        synchronized (allChannels) {
            return allChannels.size();
        }
    }

    /**
     * 释放GRPC连接池
     */
    public void dispose() {
        synchronized (this) {
            for (ManagedChannel channel : allChannels) {
                if (!channel.isShutdown()) {
                    channel.shutdown();
                }
            }
            allChannels.clear();
            freeChannels.clear();
        }
    }

    /**
     * GRPC请求
     *
     * @param msg   请求数据
     * @param retry 重试次数
     * @return 返回数据
     */
    public WechatMsg call(WechatMsg msg, int retry) {
        if (retry >= 0) {
            ManagedChannel channel = borrowChannel();//获取链接
            boolean channelOk = false;
            try {
                if (channel != null && !channel.isShutdown()) {
                    try {
                        WechatMsg rtn;
                        WechatGrpc.WechatBlockingStub stub = WechatGrpc.newBlockingStub(channel);
                        if (meta == null) {
                            rtn = stub.helloWechat(msg);
                        } else {
                            Metadata metadata = getMetaData(meta, "" + msg.getBaseMsg().getCmd());
                            WechatGrpc.WechatBlockingStub appStub = MetadataUtils.attachHeaders(stub, metadata);
                            rtn = appStub.helloWechat(msg);
                        }
                        if (rtn == null) {
                            WechatTool.delay(200);
                            return call(msg, retry - 1);
                        } else {
                            channelOk = true;
                        }
                        return rtn;
                    } catch (Exception e) {
                        WechatDebug.echo(e);
                        WechatTool.delay(200);
                        return call(msg, retry - 1);
                    }
                }
            } finally {
                if (channelOk) {
                    returnChannel(channel);//归还链接
                } else {
                    disposeChannel(channel);//释放链接
                }
            }
        }
        return null;
    }

    /**
     * 连接池配置类
     */
    public static class Config {

        public final String ip;
        public final int port;
        public final String id;
        public final String key;
        public final String token;
        public final String cert;

        public Config(String ip, int port, String id, String key, String token, String cert) {
            this.ip = ip;
            this.port = port;
            this.id = id;
            this.key = key;
            this.token = token;
            this.cert = cert;
        }

    }

}
