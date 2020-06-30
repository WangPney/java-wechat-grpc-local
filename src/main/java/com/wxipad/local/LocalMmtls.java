package com.wxipad.local;


import com.wxipad.wechat.WechatIns;
import com.wxipad.wechat.tools.crypto.Digest;
import com.wxipad.wechat.tools.tool.ToolBytes;
import com.wxipad.wechat.tools.tool.ToolStr;
import lombok.Data;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 微信MMTLS连接基础类
 */
public abstract class LocalMmtls {

    protected static final int MAX_CONNECT_RETRY = 3;//最大重试次数
    protected static final int MMTLS_BASIC_PACKS = 4;//协议握手基础包数
    protected static final int SIZE_BUFF = 1024;//缓冲取大小
    protected static final int BYTES_SIZE_RANDOM = 32;
    protected static final int BYTES_SIZE_ENCRYPT = 0x10;
    protected static final int TIMEOUT_SHORT = 15 * 1000;//短链接超时时间
    protected static final int STATUS_NEW = 0;//新建对象
    protected static final int STATUS_WORK = 3;//已经连接成功
    //各种MMTLS包头
    protected static final byte[] MMTLS_HEAD15 = ToolBytes.hex2Bytes("\\x15\\xf1\\x03");
    protected static final byte[] MMTLS_HEAD16 = ToolBytes.hex2Bytes("\\x16\\xf1\\x03");
    protected static final byte[] MMTLS_HEAD17 = ToolBytes.hex2Bytes("\\x17\\xf1\\x03");
    protected static final byte[] MMTLS_HEAD19 = ToolBytes.hex2Bytes("\\x19\\xf1\\x03");
    public final WechatIns ins;//微信实例对象
    public final String ip;//微信服务器IP
    public final int port;//微信服务器端口
    public final String id;//当前连接ID
    public final AtomicReference<KeyPair> keyPair1;//ECDH密钥对1
    public final AtomicReference<KeyPair> keyPair2;//ECDH密钥对2
    public final AtomicInteger clientSeq;//客户端封包编号
    public final AtomicInteger serverSeq;//服务端封包编号
    public final ArrayList<byte[]> digestBuffs;//用于计算HASH的数据缓冲
    public final AtomicReference<byte[]> ecdhKey;//ECDH握手密钥
    public final AtomicReference<byte[]> encryptKey;//Client加密密钥
    public final AtomicReference<byte[]> decryptKey;//Client解密密钥
    public final AtomicReference<byte[]> encryptIv;//Client加密时使用的初始化向量
    public final AtomicReference<byte[]> decryptIv;//Server解密时使用的初始化向量
    public final AtomicReference<byte[]> pskAccessKey;//PSK操作密钥
    public final AtomicReference<byte[]> pskRefreshKey;//PSK刷新密钥
    public final AtomicReference<byte[]> earlyMeta;//用于短链接的附加数据
    public final AtomicInteger versionKey;//密钥更新次数版本
    public final AtomicInteger status;//连接工作状态
    public final ToolBytes.BytesWriter recvBuff;//接收数据的缓冲
    public LocalMmtls(WechatIns ins, String ip, int port) {
        this.ins = ins;
        this.ip = ip;
        this.port = port;
        this.id = ToolStr.timeUUID();
        this.keyPair1 = new AtomicReference<>(null);
        this.keyPair2 = new AtomicReference<>(null);
        this.clientSeq = new AtomicInteger(0);
        this.serverSeq = new AtomicInteger(0);
        this.digestBuffs = new ArrayList<>();
        this.ecdhKey = new AtomicReference<>(null);
        this.encryptKey = new AtomicReference<>(null);
        this.decryptKey = new AtomicReference<>(null);
        this.encryptIv = new AtomicReference<>(null);
        this.decryptIv = new AtomicReference<>(null);
        this.pskAccessKey = new AtomicReference<>(null);
        this.pskRefreshKey = new AtomicReference<>(null);
        this.earlyMeta = new AtomicReference<>(null);
        this.versionKey = new AtomicInteger(0);
        this.status = new AtomicInteger(STATUS_NEW);
        this.recvBuff = new ToolBytes.BytesWriter(ToolBytes.BIG_ENDIAN);
    }

    /**
     * 计算Xor
     */
    protected static byte[] computeXor(byte[] iv, int seq) {
        if (iv != null && seq > 0) {
            byte[] ivXorSeq = ToolBytes.cloneBytes(iv);
            byte[] xorData = getConvert().int2bytes(seq);
            for (int i = 0; i < xorData.length; i++) {
                int index = ivXorSeq.length - xorData.length + i;
                ivXorSeq[index] = (byte) (ivXorSeq[index] ^ xorData[i]);
            }
            return ivXorSeq;
        }
        return iv;
    }

    /**
     * 封包
     *
     * @return
     */
    protected static ToolBytes.BytesWriter getPacker() {
        return new ToolBytes.BytesWriter(ToolBytes.BIG_ENDIAN);
    }

    /**
     *
     * @return
     */
    protected static ToolBytes.BytesConvert getConvert() {
        return ToolBytes.i(ToolBytes.BIG_ENDIAN);
    }

    /**
     *
     * @return
     */
    protected static int getTime() {
        long time = System.currentTimeMillis() / 1000;
        return (int) time;
    }

    /**
     *
     * @return
     */
    public boolean working() {
        return status.get() == STATUS_WORK;
    }

    /**
     *
     * @param data
     * @param get
     * @return
     */
    protected byte[] updateServerDigest(byte[] data, int get) {
        byte[] hash = null;
        synchronized (digestBuffs) {
            if (get < 0) {
                hash = LocalCrypto.genSha256(digestBuffs);
            }
            digestBuffs.add(data);
            if (get > 0) {
                hash = LocalCrypto.genSha256(digestBuffs);
            }
        }
        return hash;
    }

    /**
     *
     * @param key
     * @param info
     */
    protected void resetCurrentKeys(byte[] key, byte[] info) {
        byte[] hkdfData = LocalCrypto.genHkdf(key, info, 56);
        byte[] encryptKeyData = ToolBytes.subBytes(hkdfData, 0, 16);
        byte[] decryptKeyData = ToolBytes.subBytes(hkdfData, 16, 32);
        byte[] encryptIvData = ToolBytes.subBytes(hkdfData, 32, 44);
        byte[] decryptIvData = ToolBytes.subBytes(hkdfData, 44, 56);
        encryptKey.set(encryptKeyData);
        decryptKey.set(decryptKeyData);
        encryptIv.set(encryptIvData);
        decryptIv.set(decryptIvData);
        versionKey.incrementAndGet();
    }

    /**
     *
     * @return
     */
    protected byte[] buildClientHello() {
        //生成本地EC密钥，曲线为prime256v1
        KeyPair clientKeyPair1 = LocalCrypto.genKeyPair(LocalCrypto.CURVE_PRIME256V1);
        KeyPair clientKeyPair2 = LocalCrypto.genKeyPair(LocalCrypto.CURVE_PRIME256V1);
        this.keyPair1.set(clientKeyPair1);
        this.keyPair2.set(clientKeyPair2);
        byte[] clientPubKey1 = LocalCrypto.parseECPubKey(clientKeyPair1.getPublic(), LocalCrypto.CURVE_PRIME256V1);
        byte[] clientPubKey2 = LocalCrypto.parseECPubKey(clientKeyPair2.getPublic(), LocalCrypto.CURVE_PRIME256V1);
        byte[] clientRandom = ToolBytes.randomBytes(BYTES_SIZE_RANDOM);
        //CLIENT_HELLO封包对象
        ToolBytes.BytesWriter packer = getPacker();
        //CLIENT_HELLO封包1（打包随机数、时间戳及本地公钥）
        packer.writeBytes(ToolBytes.hex2Bytes("\\x01\\x03\\xf1\\x01\\xc0\\x2b"));
        packer.writeBytes(clientRandom);
        packer.writeUInt(getTime());
        packer.writeBytes(ToolBytes.hex2Bytes("\\x00\\x00\\x00\\xa2\\x01\\x00\\x00\\x00\\x9d\\x00\\x10\\x02\\x00\\x00\\x00\\x47\\x00\\x00\\x00\\x01"));
        packer.writeUShort(clientPubKey1.length).writeBytes(clientPubKey1);
        packer.writeBytes(ToolBytes.hex2Bytes("\\x00\\x00\\x00\\x47\\x00\\x00\\x00\\x02"));
        packer.writeUShort(clientPubKey2.length).writeBytes(clientPubKey2);
        packer.writeBytes(ToolBytes.hex2Bytes("\\x00\\x00\\x00\\x01"));
        byte[] clientHelloPack = packer.finish(true);
        //CLIENT_HELLO封包2（增加子包头）
        packer.writeUInt(clientHelloPack.length).writeBytes(clientHelloPack);
        byte[] clientHelloBody = packer.finish(true);
        updateServerDigest(clientHelloBody, 0);//更新Hash信息
        //CLIENT_HELLO封包3（增加包头）
        packer.writeBytes(MMTLS_HEAD16).writeUShort(clientHelloBody.length).writeBytes(clientHelloBody);
        byte[] clientHelloFull = packer.finish(true);
        return clientHelloFull;
    }

    /**
     *
     * @param seq
     * @param head
     * @param body
     * @throws IOException
     */
    protected void handleServerPack(int seq, byte[] head, byte[] body) throws IOException {
        byte[] data = body;
        if (seq > 0) {
            byte[] nonce = computeXor(decryptIv.get(), seq);
            byte[] aad = ToolBytes.joinBytes(getConvert().ulong2bytes(seq), head);
            data = LocalCrypto.aesGcmDecryptData(decryptKey.get(), nonce, aad, body);
        }
        if (seq == 0) {
            handleServerPack0(data);
        } else if (seq == 1) {
            handleServerPack1(data);
        } else if (seq == 2) {
            handleServerPack2(data);
        } else if (seq == 3) {
            handleServerPack3(data);
        } else if (seq >= MMTLS_BASIC_PACKS) {
            recvBuff.writeBytes(data);
            if (!handleServerData(recvBuff.finish())) {
                recvBuff.reset();
            }
        }
    }

    /**
     *
     * @param data
     * @throws IOException
     */
    protected void handleServerPack0(byte[] data) throws IOException {
        byte[] serverHash = updateServerDigest(data, 1);
        byte[] serverRandom = ToolBytes.subBytes(data, 9, 41);
        byte[] serverPub = ToolBytes.subBytes(data, 58, data.length);
        PublicKey serverPubKey = LocalCrypto.solveECPubKey(serverPub, LocalCrypto.CURVE_PRIME256V1);
        PrivateKey clientPriKey = keyPair1.get().getPrivate();
        byte[] ecdhKeyData = LocalCrypto.getShareKey(serverPubKey, clientPriKey, Digest.ALGORITHM.SHA256);
        ecdhKey.set(ecdhKeyData);
        byte[] infoData = ToolBytes.joinBytes(
                ToolBytes.hex2Bytes("\\x68\\x61\\x6e\\x64\\x73\\x68\\x61\\x6b\\x65\\x20\\x6b\\x65\\x79\\x20\\x65\\x78\\x70\\x61\\x6e\\x73\\x69\\x6f\\x6e"),
                serverHash
        );
        resetCurrentKeys(ecdhKeyData, infoData);
    }

    /**
     *
     * @param data
     * @throws IOException
     */
    protected void handleServerPack1(byte[] data) throws IOException {
        byte[] serverHash = updateServerDigest(data, 1);
        byte[] infoPskAccess = ToolBytes.joinBytes(
                ToolBytes.hex2Bytes("\\x50\\x53\\x4b\\x5f\\x41\\x43\\x43\\x45\\x53\\x53"),
                serverHash
        );
        byte[] infoPskRefresh = ToolBytes.joinBytes(
                ToolBytes.hex2Bytes("\\x50\\x53\\x4b\\x5f\\x52\\x45\\x46\\x52\\x45\\x53\\x48"),
                serverHash
        );
        byte[] pskAccessKeyData = LocalCrypto.genHkdf(ecdhKey.get(), infoPskAccess, 32);
        byte[] pskRefreshKeyData = LocalCrypto.genHkdf(ecdhKey.get(), infoPskRefresh, 32);
        pskAccessKey.set(pskAccessKeyData);
        pskRefreshKey.set(pskRefreshKeyData);
    }

    /**
     *
     * @param data
     * @throws IOException
     */
    protected void handleServerPack2(byte[] data) throws IOException {
        updateServerDigest(data, 0);
        int earlyMetaLen = (int) getConvert().bytes2uint(data, 6);
        byte[] earlyMetaData = ToolBytes.subBytes(data, 6, 6 + 4 + earlyMetaLen);
        earlyMeta.set(earlyMetaData);
    }

    /**
     *
     * @param data
     * @throws IOException
     */
    protected void handleServerPack3(byte[] data) throws IOException {
        byte[] serverOldHash = updateServerDigest(data, -1);
        //发送CLIENT_FINISH数据包
        handleClientFinish(serverOldHash);
        //计算最后协商ECDH密钥
        byte[] infoData1 = ToolBytes.joinBytes(
                ToolBytes.hex2Bytes("\\x65\\x78\\x70\\x61\\x6e\\x64\\x65\\x64\\x20\\x73\\x65\\x63\\x72\\x65\\x74"),
                serverOldHash
        );
        byte[] infoData2 = ToolBytes.joinBytes(
                ToolBytes.hex2Bytes("\\x61\\x70\\x70\\x6c\\x69\\x63\\x61\\x74\\x69\\x6f\\x6e\\x20\\x64\\x61\\x74\\x61\\x20\\x6b\\x65\\x79\\x20\\x65\\x78\\x70\\x61\\x6e\\x73\\x69\\x6f\\x6e"),
                serverOldHash
        );
        byte[] ecdhKeyData = LocalCrypto.genHkdf(ecdhKey.get(), infoData1, 32);
        ecdhKey.set(ecdhKeyData);
        resetCurrentKeys(ecdhKeyData, infoData2);
        digestBuffs.clear();//清空计算HASH的数据缓冲
        status.set(STATUS_WORK);//TLS握手完成
    }

    /**
     *
     * @param data
     * @return
     * @throws IOException
     */
    protected boolean handleServerData(byte[] data) throws IOException {
        //用于长连接获取后续数据
        return false;//是否有后续数据
    }

    /**
     *
     * @param serverHash
     * @throws IOException
     */
    protected void handleClientFinish(byte[] serverHash) throws IOException {
        //用于长连接发送CLIENT_FINISH数据包
    }

    @Data
    public static class LongPackHead {
        public int packLength;      // 4字节封包长度(含包头)，可变
        public short headLength = 16;      // 2字节表示头部长度,固定值，0x10
        public short clientVersion = 1;   // 2字节表示协议版本，固定值，0x01
        public int cmdid;           // 4字节cmdid，可变
        public int seq;              // 4字节封包编号，可变
    }

    private static byte[] handerTransfer(LongPackHead longPackHead) {
        byte[] headerBuf = new byte[16];
        apeendInt2ByteArr(headerBuf, longPackHead.packLength, 0);
        apeendShort2ByteArr(headerBuf, longPackHead.headLength, 4);
        apeendShort2ByteArr(headerBuf, longPackHead.clientVersion, 6);
        apeendInt2ByteArr(headerBuf, longPackHead.cmdid, 8);
        apeendInt2ByteArr(headerBuf, longPackHead.seq, 12);
        return headerBuf;
    }

    public static byte[] makeLongPack(byte[] body,int cmd) {
        LongPackHead longPackHead = new LongPackHead();
        longPackHead.packLength = body.length + 16;
        longPackHead.headLength = 16;
        longPackHead.clientVersion = 1;
        longPackHead.cmdid = cmd;
        longPackHead.seq = Math.abs(new Random().nextInt());
        return handerTransfer(longPackHead);
    }

    private static void apeendInt2ByteArr(byte[] bytes, int val, int offset) {
        bytes[offset + 3] = (byte) (val & 0xFF);
        bytes[offset + 2] = (byte) ((val >> 8) & 0xFF);
        bytes[offset + 1] = (byte) ((val >> 16) & 0xFF);
        bytes[offset] = (byte) ((val >> 24) & 0xFF);
    }

    private static void apeendShort2ByteArr(byte[] bytes, short val, int offset) {
        bytes[offset + 1] = (byte) (val & 0xFF);
        bytes[offset] = (byte) ((val >> 8) & 0xFF);
    }
}
