package com.wxipad.local;


import com.alibaba.fastjson.JSON;
import com.wxipad.local.WechatProto.*;
import com.wxipad.wechat.WechatConst;
import com.wxipad.wechat.WechatDebug;
import com.wxipad.wechat.WechatIns;
import com.wxipad.wechat.tools.tool.ToolBytes;
import com.wxipad.wechat.tools.tool.ToolStream;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * 微信短连接
 */
@Slf4j
public class LocalShort extends LocalMmtls {

    public LocalShort(WechatIns ins, String ip, int port) {
        super(ins, ip, port);
    }

    public boolean dispose() {
        return true;
    }

    /**
     * 初始化
     *
     * @return
     */
    public boolean init() {
        int retry = MAX_CONNECT_RETRY;
        while (retry > 0) {
            try {
                byte[] clientHelloFull = buildClientHello();
                byte[] recvData = sendPlainData(clientHelloFull);
                int pos = 0, seq = 0;
                while (pos + 5 < recvData.length && seq < MMTLS_BASIC_PACKS) {
                    int length = getConvert().bytes2ushort(recvData, pos + 3);
                    byte[] head = ToolBytes.cutBytes(recvData, pos, 5);
                    byte[] body = ToolBytes.cutBytes(recvData, pos + 5, length);
                    handleServerPack(seq, head, body);
                    pos += 5 + length;
                    seq++;
                }
                return seq == MMTLS_BASIC_PACKS //验证数据包完整性
                        && status.get() == STATUS_WORK;//判断连接工作状态
            } catch (Exception e) {
                WechatDebug.echo(e);
            }
            retry--;
        }
        return false;
    }


    /**
     * 新的同步请求
     * @return
     * @throws IOException
     */
    public NewSyncResponse sendNewSyncRequest() throws IOException {
        byte[] data = WechatUtil.packNewSyncRequest(ins);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/newsync", data);
        return WechatUtil.unpackNewSyncResponse(ins, result);
    }

    /**
     * 发送普通文本消息
     * @param wxid
     * @param content
     * @param at
     * @return
     * @throws IOException
     */
    public SendMsgResponseNew sendSendMsg(String wxid, String content, String[] at) throws IOException {
        return sendMsg(wxid, content, at, 1);
    }

    /**
     * 发送app消息
     * @param wxid
     * @param content
     * @param at
     * @return
     * @throws IOException
     */
    public SendMsgResponseNew sendAppMsg(String wxid, String content,String[] at) throws IOException {
        byte[] data = WechatUtil.packSendMsgRequestNew(ins, wxid, content, 5, at);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/sendappmsg", data);
        SendMsgResponseNew sendMsgResponseNew = WechatUtil.unpackSendMsgResponseNew(ins, result);
        System.out.println("主动发送app消息回调 => " + JSON.toJSONString(sendMsgResponseNew));
        return sendMsgResponseNew;
    }

    /**
     * 发送名片消息
     * @param wxid
     * @param content
     * @param at
     * @return
     * @throws IOException
     */
    public SendMsgResponseNew sendWxUserCardMsg(String wxid, String content,String[] at) throws IOException {
        return sendMsg(wxid, content, at, 42);
    }

    /**
     * 发消息
     * @param wxid
     * @param content
     * @param at
     * @return
     * @throws IOException
     */
    public SendMsgResponseNew sendMsg(String wxid, String content, String[] at,int type) throws IOException {
        byte[] data = WechatUtil.packSendMsgRequestNew(ins, wxid, content, type, at);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/newsendmsg", data);
        SendMsgResponseNew sendMsgResponseNew = WechatUtil.unpackSendMsgResponseNew(ins, result);
        System.out.println("主动发送消息回调 => " + JSON.toJSONString(sendMsgResponseNew));
        return sendMsgResponseNew;
    }

    /**
     * 发送xml
     * @param xml
     * @return
     * @throws IOException
     */
    public SnsPostResponse sendSnsPost(String xml) throws IOException {
        byte[] data = WechatUtil.packSnsPostRequest(ins, xml);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/mmsnspost", data);
        return WechatUtil.unpackSnsPostResponse(ins, result);
    }

    /**
     * 朋友圈评论
     * @param id
     * @param wxid
     * @param content
     * @return
     * @throws IOException
     */
    public SnsCommentResponse sendSnsComment(String id, String wxid, String content) throws IOException {
        byte[] data = WechatUtil.packSnsCommentRequest(ins, id, wxid, content);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/mmsnscomment", data);
        return WechatUtil.unpackSnsCommentResponse(ins, result);
    }

    /**
     * 搜索联系人
     * @param wxid
     * @return
     * @throws IOException
     */
    public SearchContactResponse sendSearchContact(String wxid) throws IOException {
        byte[] data = WechatUtil.packSearchContactRequest(ins, wxid);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/searchcontact", data);
        return WechatUtil.unpackSearchContactResponse(ins, result);
    }

    /**
     * 面对面建群
     * @param passWord
     * @param longitude
     * @param latitude
     * @return
     * @throws IOException
     * 比如说你传baesMasg进来，处理完data就赋值到对应的字段，cmdurl一样的赋值进去，返回wechatMsg
     */
    public FacingCreateChatRoomResponse sendFacingCreateChatRoom(String passWord, Float longitude, Float latitude) throws IOException {
        byte[] data = WechatUtil.packFacingCreateChatRoomRequest(ins, 0, passWord, longitude, latitude);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/mmfacingcreatechatroom", data);
        FacingCreateChatRoomResponse response = WechatUtil.unpackFacingCreateChatRoomResponse(ins, result);
        log.debug("面对面建群第一次请求结果 ==> {}", JSON.toJSONString(response));
        data = WechatUtil.packFacingCreateChatRoomRequest(ins, 1, passWord, longitude, latitude);
        result = sendMmtlsData("/cgi-bin/micromsg-bin/mmfacingcreatechatroom", data);
        response = WechatUtil.unpackFacingCreateChatRoomResponse(ins, result);
        log.debug("面对面建群第二次请求结果 ==> {}", JSON.toJSONString(response));

        return response;
    }

    /**
     * 设置聊天室公告
     *
     * @param chatroom
     * @param announcement
     * @return
     * @throws IOException
     */
    public SetChatRoomAnnouncementResponse setChatRoomAnnouncement(String chatroom, String announcement) throws IOException {
        byte[] data = WechatUtil.packSetChatRoomAnnouncementRequest(ins, chatroom, announcement);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/setchatroomannouncement", data);
        return WechatUtil.unpackSetChatRoomAnnouncementResponse(ins, result);
    }
    /**
     *
     * @param v1
     * @param v2
     * @return
     * @throws IOException
     */
    public VerifyUserResponse sendVerifyUser(String v1, String v2) throws IOException {
        return sendVerifyUser(v1, v2, 3, 6, "", "");
    }

    /**
     * 添加好友
     * @param v1
     * @param v2
     * @param content
     * @return
     * @throws IOException
     */
    public VerifyUserResponse sendVerifyUser(String v1, String v2, String content) throws IOException {
        return sendVerifyUser(v1, v2, 3, 6, content, "");
    }

    /**
     * 添加群好友
     * @param v1
     * @param v2
     * @param content
     * @param chatRoomUserName
     * @return
     * @throws IOException
     */
    public VerifyUserResponse sendVerifyUser(String v1, String v2, String content, String chatRoomUserName) throws IOException {
        return sendVerifyUser(v1, v2, 3, 6, content, chatRoomUserName);
    }

    /**
     * 添加好友
     * @param v1
     * @param v2
     * @param opcode
     * @param scene
     * @param content
     * @param chatRoomUserName
     * @return
     * @throws IOException
     */
    public VerifyUserResponse sendVerifyUser(String v1, String v2, int opcode, int scene, String content, String chatRoomUserName) throws IOException {
        byte[] data = WechatUtil.packVerifyUserRequest(ins, v1, v2, opcode, scene, content, chatRoomUserName, "", "");
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/verifyuser", data);
        return WechatUtil.unpackVerifyUserResponse(ins, result);
    }

    /**
     * 添加好友
     * @param v1
     * @param v2
     * @param opcode
     * @param scene
     * @param content
     * @return
     * @throws IOException
     */
    public VerifyUserResponse sendVerifyUser(String v1, String v2, int opcode, int scene, String content) throws IOException {
        byte[] data = WechatUtil.packVerifyUserRequest(ins, v1, v2, opcode, scene, content);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/verifyuser", data);
        return WechatUtil.unpackVerifyUserResponse(ins, result);
    }

    /**
     * 删除群成员
     * @param chatroom
     * @param wxid
     * @return
     * @throws IOException
     */
    public DelTalkRoomMemberResponse sendDelTalkRoomMember(String chatroom, String wxid) throws IOException {
        byte[] data = WechatUtil.packDelTalkRoomMemberRequest(ins, chatroom, wxid);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/delchatroommember", data);
        return WechatUtil.unpackDelTalkRoomMemberResponse(ins, result);
    }

    /**
     * 发送图片
     * @param wxid
     * @param fileId
     * @param fileKey
     * @param imgSize
     * @param midSize
     * @param thumbSize
     * @param aesKey
     * @param fileMd5
     * @param checkSum
     * @return
     * @throws IOException
     */
    public UploadMsgImgResponse sendSendImg(String wxid, String fileId, String fileKey, int imgSize, int midSize, int thumbSize, String aesKey, String fileMd5, int checkSum) throws IOException {
        byte[] data = WechatUtil.packUploadMsgImgRequest(ins, wxid, fileId, fileKey, imgSize, midSize, thumbSize, aesKey, fileMd5, checkSum);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/uploadmsgimg", data);
        return WechatUtil.unpackUploadMsgImgResponse(ins, result);
    }

    /**
     * 发送图片
     * @param wxid
     * @param id
     * @param pos
     * @param len
     * @param part
     * @return
     * @throws IOException
     */
    public UploadMsgImgResponse sendSendImg(String wxid, String id, int pos, int len, byte[] part) throws IOException {
        byte[] data = WechatUtil.packUploadMsgImgRequest(ins, wxid, id, pos, len, part);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/uploadmsgimg", data);
        return WechatUtil.unpackUploadMsgImgResponse(ins, result);
    }

    /**
     * 发送视频
     * @param wxid
     * @param fileId
     * @param fileKey
     * @param videoLength
     * @param playLength
     * @param thumbLength
     * @param aesKey
     * @param fileMd5
     * @param fileNewMd5
     * @param checkSum
     * @return
     * @throws IOException
     */
    public UploadVideoResponse sendSendVideo(String wxid, String fileId, String fileKey, int videoLength, int playLength, int thumbLength, String aesKey, String fileMd5, String fileNewMd5, int checkSum) throws IOException {
        byte[] data = WechatUtil.packUploadVideoRequest(ins, wxid, fileId, fileKey, videoLength, playLength, thumbLength, aesKey, fileMd5, fileNewMd5, checkSum);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/uploadvideo", data);
        return WechatUtil.unpackUploadVideoResponse(ins, result);
    }

    /**
     * 朋友圈上传
     * @param id
     * @param pos
     * @param len
     * @param part
     * @return
     * @throws IOException
     */
    public SnsUploadResponse sendSnsUpload(String id, int pos, int len, byte[] part) throws IOException {
        byte[] data = WechatUtil.packSnsUploadRequest(ins, id, pos, len, part);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/mmsnsupload", data);
        return WechatUtil.unpackSnsUploadResponse(ins, result);
    }

    public byte[] sendPlainData(byte[] data) throws IOException {
        String cgi = "/mmtls/" + Integer.toHexString(getTime());
        URL url = new URL("http://" + ip + ":" + port + cgi);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        byte[] responseData = null;
        try {
            conn.setConnectTimeout(TIMEOUT_SHORT);
            conn.setReadTimeout(TIMEOUT_SHORT);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Content-type", "application/octet-stream");
            conn.setRequestProperty("User-Agent", "MicroMessenger Client");
            byte[] requestData = data;
            if (requestData != null) {
                conn.setRequestProperty("Content-Length", Integer.toString(requestData.length));
                OutputStream os = conn.getOutputStream();
                try {
                    os.write(requestData);
                } finally {
                    os.close();
                }
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String contentEncoding = conn.getHeaderField("Content-Encoding");
                boolean gzip = contentEncoding != null && contentEncoding.toLowerCase().equals("gzip");
                boolean deflate = contentEncoding != null && contentEncoding.toLowerCase().equals("deflate");
                InputStream is = conn.getInputStream();
                try {
                    if (gzip) {
                        is = new GZIPInputStream(is);
                    }
                    if (deflate) {
                        is = new InflaterInputStream(is, new Inflater(true));
                    }
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        ToolStream.pipe(is, out, SIZE_BUFF);
                        responseData = out.toByteArray();
                    } finally {
                        out.close();
                    }
                } finally {
                    is.close();
                }
            }
        } finally {
            conn.disconnect();
        }
        return responseData;
    }

    /**
     * 心跳
     *
     * @return
     * @throws IOException
     */
    public HeartBeatResponse sendHeartBeat() throws IOException {
        byte[] data = WechatUtil.packHeartBeatRequest(ins);
        byte[] result = sendMmtlsData("/cgi-bin/micromsg-bin/heartbeat", data);
        return WechatUtil.unpackHeartBeatResponse(ins, result);
    }

    public byte[] sendMmtlsData(String url, byte[] data) throws IOException {
        byte[] urlBytes = url.getBytes(WechatConst.CHARSET);
        byte[] hostBytes = "short.weixin.qq.com".getBytes(WechatConst.CHARSET);
        ToolBytes.BytesWriter packer = getPacker();
        packer.writeUShort(urlBytes.length).writeBytes(urlBytes);
        packer.writeUShort(hostBytes.length).writeBytes(hostBytes);
        packer.writeUInt(data.length).writeBytes(data);
        byte[] sendData = packer.finish(true);
        packer.writeUInt(sendData.length).writeBytes(sendData);
        byte[] fullData = packer.finish(true);
        ArrayList<byte[]> hashDatas = new ArrayList<>();
        //加密数据
        byte[] encryptData = mmtlsEncryptData(fullData, hashDatas);
        //发送并接受数据
        byte[] rtnData = sendPlainData(encryptData);
        //解密数据
        byte[] decryptData = mmtlsDecryptData(rtnData, hashDatas);
        return decryptData;
    }

    private byte[] mmtlsEncryptData(byte[] data, ArrayList<byte[]> hashDatas) throws IOException {
        ToolBytes.BytesWriter packer = getPacker();
        //SEQ=0，头部数据
        packer.writeBytes(ToolBytes.hex2Bytes("\\x01\\x03\\xf1\\x01\\x00\\xa8"));
        packer.writeBytes(ToolBytes.randomBytes(BYTES_SIZE_RANDOM)).writeUInt(getTime());
        packer.writeBytes(ToolBytes.hex2Bytes("\\x00\\x00\\x00\\x6f\\x01\\x00\\x00\\x00\\x6a\\x00\\x0f\\x01"));
        packer.writeBytes(earlyMeta.get());
        byte[] shortPack0 = packer.finish(true);
        packer.writeUInt(shortPack0.length).writeBytes(shortPack0);
        byte[] shortBody0 = packer.finish(true);
        packer.writeBytes(MMTLS_HEAD19).writeUShort(shortBody0.length).writeBytes(shortBody0);
        byte[] shortFull0 = packer.finish(true);
        //加密密钥生成
        hashDatas.add(shortBody0);
        byte[] serverHash0 = LocalCrypto.genSha256(hashDatas);
        byte[] handshakeData = ToolBytes.joinBytes(
                ToolBytes.hex2Bytes("\\x65\\x61\\x72\\x6c\\x79\\x20\\x64\\x61\\x74\\x61\\x20\\x6b\\x65\\x79\\x20\\x65\\x78\\x70\\x61\\x6e\\x73\\x69\\x6f\\x6e"),
                serverHash0
        );
        byte[] shortEncryptHkdf = LocalCrypto.genHkdf(pskAccessKey.get(), handshakeData, 28);
        byte[] shortEncryptKey = ToolBytes.subBytes(shortEncryptHkdf, 0, 16);
        byte[] shortEncryptIv = ToolBytes.subBytes(shortEncryptHkdf, 16, 28);
        //SEQ=1~3,业务数据
        packer.writeBytes(ToolBytes.hex2Bytes("\\x00\\x00\\x00\\x10\\x08\\x00\\x00\\x00\\x0b\\x01\\x00\\x00\\x00\\x06\\x00\\x12"));
        packer.writeUInt(getTime());
        byte[] shortBody1 = packer.finish(true);
        byte[] shortBody2 = data;
        byte[] shortBody3 = ToolBytes.hex2Bytes("\\x00\\x00\\x00\\x03\\x00\\x01\\x01");
        byte[][] shortHeads = {null, MMTLS_HEAD19, MMTLS_HEAD17, MMTLS_HEAD15};
        byte[][] shortBodys = {null, shortBody1, shortBody2, shortBody3};
        byte[][] shortFulls = {shortFull0, null, null, null};
        for (int seq = 1; seq < MMTLS_BASIC_PACKS; seq++) {
            byte[] shortHead = shortHeads[seq];
            byte[] shortBody = shortBodys[seq];
            byte[] shortNonce = computeXor(shortEncryptIv, seq);
            byte[] shortAad = ToolBytes.joinBytes(getConvert().ulong2bytes(seq),
                    shortHead, getConvert().ushort2bytes(shortBody.length + BYTES_SIZE_ENCRYPT));
            byte[] encryptData = LocalCrypto.aesGcmEncryptData(shortEncryptKey, shortNonce, shortAad, shortBody);
            packer.writeBytes(shortHead).writeUShort(shortBody.length + BYTES_SIZE_ENCRYPT).writeBytes(encryptData);
            byte[] shortFull = packer.finish(true);
            shortFulls[seq] = shortFull;
        }
        //整合所有数据包，用于MMTLS二次连接数据
        for (byte[] shortFull : shortFulls) {
            packer.writeBytes(shortFull);
        }
        byte[] mmtlsSecondData = packer.finish(true);
        hashDatas.add(shortBody1);
        return mmtlsSecondData;
    }

    private byte[] mmtlsDecryptData(byte[] data, ArrayList<byte[]> hashDatas) throws IOException {
        int pos = 0, seq = 0, length = 0;
        byte[] shortHead = null, shortBody = null;
        byte[] shortDecryptKey = null, shortDecryptIv = null;
        byte[] businessData = null;
        while (pos + 5 < data.length && seq < MMTLS_BASIC_PACKS) {
            length = getConvert().bytes2ushort(data, pos + 3);
            shortHead = ToolBytes.cutBytes(data, pos, 5);
            shortBody = ToolBytes.cutBytes(data, pos + 5, length);
            if (seq == 0) {
                hashDatas.add(shortBody);
                byte[] serverHash0 = LocalCrypto.genSha256(hashDatas);
                byte[] handshakeData = ToolBytes.joinBytes(
                        ToolBytes.hex2Bytes("\\x68\\x61\\x6e\\x64\\x73\\x68\\x61\\x6b\\x65\\x20\\x6b\\x65\\x79\\x20\\x65\\x78\\x70\\x61\\x6e\\x73\\x69\\x6f\\x6e"),
                        serverHash0
                );
                byte[] shortDecryptHkdf = LocalCrypto.genHkdf(pskAccessKey.get(), handshakeData, 28);
                shortDecryptKey = ToolBytes.subBytes(shortDecryptHkdf, 0, 16);
                shortDecryptIv = ToolBytes.subBytes(shortDecryptHkdf, 16, 28);
            } else {
                byte[] shortNonce = computeXor(shortDecryptIv, seq);
                byte[] shortAad = ToolBytes.joinBytes(getConvert().ulong2bytes(seq), shortHead);
                byte[] decryptData = LocalCrypto.aesGcmDecryptData(shortDecryptKey, shortNonce, shortAad, shortBody);
                if (seq == 2) {
                    businessData = decryptData;//只获取第二部分的业务数据
                }
            }
            pos += 5 + length;
            seq++;
        }
        return seq == MMTLS_BASIC_PACKS ? businessData : null;
    }

}
