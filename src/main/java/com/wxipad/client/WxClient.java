package com.wxipad.client;

import com.wxipad.wechat.WechatConst;
import com.wxipad.wechat.WechatDebug;
import com.wxipad.wechat.WechatIns;
import com.wxipad.wechat.WechatTool;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class WxClient implements Runnable {
    protected static final int STATUS_NEW = 0;//新建对象
    protected static final int STATUS_OPEN = 1;//长连接打开
    protected static final int STATUS_WORK = 2;//已经连接成功
    protected static final int STATUS_STOP = -1;//连接停止工作
    protected static final int STATUS_ERROR = -2;//发生错误
    private static final int SOCKET_RETRY = 3;//最大重试次数
    private static final int SOCKET_TIMEOUT = 3 * 60 * 1000;//读写超时时间
    private static final long TIMER_LOOP_MS = 90 * 1000l;//心跳包发送间隔
    private static final AtomicLong clientCounter = new AtomicLong(0);//线程计数器
    private static final AtomicLong syncCounter = new AtomicLong(0);//线程计数器
    public final WechatIns ins;//微信实例对象
    public final String ip;//微信服务器IP
    public final int port;//微信服务器端口
    public final long id;//当前连接ID
    private final AtomicInteger status;//连接工作状态
    private final AtomicReference<Timer> timer;//心跳定时器
    private final ConcurrentHashMap<Integer, Callback> callbacks;//回调缓存
    private final Socket socket;//当前连接SOCK
    private final InputStream in;//当前连接输入
    private final OutputStream out;//当前连接输出

    /**
     * 构造微信长连接
     *
     * @param ins  微信实例
     * @param ip   微信服务器IP
     * @param port 微信服务器端口
     */
    public WxClient(WechatIns ins, String ip, int port) {
        this.ins = ins;
        this.ip = ip;
        this.port = port;
        this.id = clientCounter.incrementAndGet();
        this.status = new AtomicInteger(STATUS_NEW);
        this.timer = new AtomicReference<>(null);
        this.callbacks = new ConcurrentHashMap<>();
        Socket socket = null;
        int retry = SOCKET_RETRY;
        while (socket == null && retry > 0) {
            try {
                socket = new Socket(ip, port);
                socket.setSoTimeout(SOCKET_TIMEOUT);
            } catch (IOException e) {
                WechatDebug.echo(e);
            }
            retry--;
        }
        this.socket = socket;
        InputStream in = null;
        OutputStream out = null;
        if (socket != null) {
            try {
                in = socket.getInputStream();
            } catch (IOException e) {
                WechatDebug.echo(e);
            }
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                WechatDebug.echo(e);
            }
        }
        this.in = in;
        this.out = out;
        boolean success = socket != null && in != null && out != null;
        status.set(success ? STATUS_OPEN : STATUS_ERROR);
    }

    /**
     * 判断长连接是否处在工作状态
     *
     * @return 是否为工作状态
     */
    public boolean working() {
        return status.get() == STATUS_WORK;
    }

    /**
     * 启动微信长连接
     *
     * @return 是否启动成功
     */
    public boolean startup() {
        if (status.compareAndSet(STATUS_OPEN, STATUS_WORK)) {
            try {
                //发送首个心跳包
                startHeartbeat();
                //初始化连接完成，启动接收线程
                String name = "LongRecv-" + id;
                new Thread(this, name).start();
                return true;
            } catch (Exception e) {
                WechatDebug.echo(e);
                status.set(STATUS_ERROR);
            }
        }
        return false;
    }

    /**
     * 关闭微信长连接
     */
    public void shutdown() {
        if (status.compareAndSet(STATUS_WORK, STATUS_STOP)) {
            stopHeartbeat();
            if (in != null) {
                try {
                    in.close();//关闭输入
                } catch (IOException e) {
                    WechatDebug.echo(e);
                }
            }
            if (out != null) {
                try {
                    out.close();//关闭输出
                } catch (IOException e) {
                    WechatDebug.echo(e);
                }
            }
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();//关闭连接
                } catch (IOException e) {
                    WechatDebug.echo(e);
                }
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data     要发送的数据字节数组
     * @param callback 指定回调函数，传null则无回调
     * @throws IOException
     */
    public void sendData(byte[] data, Callback callback) throws IOException {
        if (callback != null) {
            int reqSeq = WechatTool.bytesToInt(data, 12);
            callbacks.put(reqSeq, callback);
        }
        synchronized (out) {
            out.write(data);
            out.flush();
        }
    }

    /**
     * 开始定时心跳
     */
    public void startHeartbeat() {
        Timer newTimer = new Timer("HeartBeat-" + id);
        Timer oldTimer = timer.getAndSet(newTimer);
        if (oldTimer != null) {
            oldTimer.cancel();
        }
        newTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (working()) {
                    try {
                        if (ins.logined() && ins.online()) {
                            ins.doHeartbeat();
                        }
                    } catch (Exception e) {
                        WechatDebug.echo(e);
                        status.set(STATUS_ERROR);
                    }
                }
            }
        }, TIMER_LOOP_MS, TIMER_LOOP_MS);
    }

    /**
     * 停止定时心跳
     */
    public void stopHeartbeat() {
        Timer oldTimer = timer.getAndSet(null);
        if (oldTimer != null) {
            oldTimer.cancel();
        }
    }

    /**
     * 长连接数据接收线程
     */
    @Override
    public void run() {
        while (working()) {
            try {
                byte[] head = WechatTool.needBytes(in, 16);
                if (head == null) {
                    status.set(STATUS_STOP);
                    break;//读取数据结束
                }
                int length = WechatTool.bytesToInt(head, 0);
                byte[] body = WechatTool.needBytes(in, length - head.length);
                if (head != null && body != null) {
                    byte[] data = WechatTool.joinBytes(head, body);
                    if (data.length == 20 && data[3] == 20 && data[5] == 16 && data[7] == 1) {
                        //新消息通知包
                        String name = "NewSync-" + syncCounter.incrementAndGet();
                        WechatDebug.log(ins, "WxClient.run", name);
                        final boolean offline = ins.online() && data.length == 20
                                && WechatTool.bytesToInt(data, 16) == -1;
                        new Thread(() -> {
                            if (WechatConst.OFFLINE && offline) {
                                ins.longConn().shutdown();//关闭长连接
                                ins.online.set(0l);
                                ins.callback.offline(ins);
                            } else {
                                ins.newSync();
                            }
                        }, name).start();
                    }
                    //收到可解包数据
                    else if (data[16] == -65) {
                        int reqSeq = WechatTool.bytesToInt(data, 12);
                        final Callback callback = callbacks.remove(reqSeq);
                        if (callback != null) {
                            callback.onData(data);
                            synchronized (callback) {
                                callback.notifyAll();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (status.get() != STATUS_STOP) {
                    WechatDebug.echo(e);
                    status.set(STATUS_ERROR);
                }
            }
        }
    }

    /**
     * 长连接请求回调函数
     */
    public abstract static class Callback {
        public abstract void onData(byte[] data);
    }


}
