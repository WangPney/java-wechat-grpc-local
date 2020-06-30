package com.wxipad.local;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.wxipad.local.WechatProto.*;
import com.wxipad.web.ApiCmd;
import com.wxipad.wechat.WechatConst;
import com.wxipad.wechat.WechatCtx;
import com.wxipad.wechat.WechatDebug;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import com.wxipad.wechat.tools.crypto.BASE64;
import com.wxipad.wechat.tools.proto.ProtoData;
import com.wxipad.wechat.tools.proto.ProtoException;
import com.wxipad.wechat.tools.tool.ToolBytes;
import com.wxipad.wechat.tools.tool.ToolDate;
import com.wxipad.wechat.tools.tool.ToolStr;
import com.wxipad.wechat.tools.tool.ToolZip;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

@Slf4j
public class WechatUtil {

    /**
     * 生成appMsg xml
     *
     * @param title    标题
     * @param content  内容
     * @param pointUrl 指向url
     * @param thumburl 缩略图url
     * @return
     */
    public static String getAppMsgXml(String title, String content, String pointUrl, String thumburl) {
        StringBuilder sb = new StringBuilder("<appmsg> ");
        sb.append("<title>").append(title).append("</title>");
        sb.append("<des>").append(content).append("</des>");
        sb.append("<action>").append("view").append("</action>");
        sb.append("<type>").append(5).append("</type>");
        sb.append("<showtype>").append(0).append("</showtype>");
        sb.append("<url>").append(pointUrl).append("</url>");
        sb.append("<thumburl>").append(thumburl).append("</thumburl>");
        sb.append("</appmsg>");
        return sb.toString();
    }

    /**
     * 发送名片xml
     */
    public static String getWxUserCardXml(String userName, String nickName, String province, String city, int sex){
        return "<msg " +
                "  username=\"" + userName +"\" " +
                "  nickname=\"" + nickName + "\" " +
                "  fullpy=\"fanyingcongcong\" " +
                "  shortpy=\"FYCC\" " +
                "  alias=\"\" " +
                "  imagestatus=\"3\" " +
                "  scene=\"17\" " +
                "  province=\"" + province + "\" " +
                "  city=\"" + city +"\" " +
                "  sign=\"\" " +
                "  sex=\"" + sex + "\" " +
                "  certflag=\"0\" " +
                "  certinfo=\"\" " +
                "  brandIconUrl=\"\" " +
                "  brandHomeUrl=\"\" " +
                "  brandSubscriptConfigUrl=\"\" " +
                "  brandFlags=\"0\" " +
                "  regionCode=\"\">" +
                "</msg>";
    }

    public static final byte[] CHECK_DATA_BYTES = new byte[]{
            0x0a, 0x08, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x33, 0x10, 0x01, 0x1a, 0x00
    };

    public static byte[] unpack(WechatCtx ctx, byte[] data) {
        return unpack(ctx, data, null);
    }

    public static byte[] unpack(WechatCtx ctx, byte[] data, byte[] key) {
        byte[] aesKey = key != null ? key : ctx.getSessionKey();
        ToolBytes.BytesReader reader = new ToolBytes.BytesReader(data, ToolBytes.BIG_ENDIAN);
        short temp;
        if (data[0] == (byte) 0xbf) {
            temp = reader.readUByte();//跳过协议标志位
        }
        temp = reader.readUByte();
        int headerLen = temp >> 2;
        boolean compress = (temp & 0x03) == 1;
        temp = reader.readUByte();
        int decrypt = temp >> 4;//解密算法(固定为AES解密):05-AES解密/07-RSA解密
        int cookieLen = temp & 0x0f;//Cookie长度
        int serverVersion = reader.readInt();//服务器版本
        long uin = reader.readUInt();//用户UIN
        byte[] cookieData = reader.readBytes(cookieLen);//获取Cookie
        int skip = 0, cgi = 0, originLen = 0, compressLen = 0;
        try {
            cgi = ProtoData.bytes2varint32(data, reader.getCursor());
            skip = ProtoData.testVarintLength(data, reader.getCursor());
            reader.skip(skip);
            originLen = ProtoData.bytes2varint32(data, reader.getCursor());
            skip = ProtoData.testVarintLength(data, reader.getCursor());
            reader.skip(skip);
            compressLen = ProtoData.bytes2varint32(data, reader.getCursor());
            skip = ProtoData.testVarintLength(data, reader.getCursor());
            reader.skip(skip);
        } catch (ProtoException e) {
            e.printStackTrace();
            return null;
        }
        byte[] body = ToolBytes.subBytes(data, headerLen, data.length);
        if (body.length > 0) {
            byte[] buff = LocalCrypto.aesCbcDecryptData(aesKey, aesKey, body);
            if (buff != null && compress) {
                buff = ToolZip.decompress(buff);
            }
            return buff;
        } else {
            return body;
        }
    }

    public static byte[] pack(WechatCtx ctx, int cgi, byte[] data, boolean compress) {
        byte[] ecdhKey = ctx.getSessionKey();//这里可能有问题
        ToolBytes.BytesWriter writer = new ToolBytes.BytesWriter(ToolBytes.BIG_ENDIAN);
        //header[1]=后2bit，02:包体不使用压缩算法;前6bit，包头长度，最后计算
        //header[2]=前4bit，07:RSA加密算法,05:AES加密算法;后4bit，0xf:cookie长度
        int originLen = data.length, compressLen = data.length;
        byte[] body = data;
        if (compress) {
            body = ToolZip.compress(body);
            compressLen = body.length;
        }
        byte[] aesKey = ctx.getSessionKey();
        body = LocalCrypto.aesCbcEncryptData(aesKey, aesKey, body);
        writer.writeBytes(ToolBytes.hex2Bytes("\\xbf\\x00\\x5f"));
        writer.writeUInt(ctx.genClientVersion());//客户端版本号
        writer.writeUInt(ctx.getUin());//用户UIN
        writer.writeBytes(ctx.getCookies());
        writer.writeBytes(ProtoData.varint2bytes(cgi));//CIG类型
        writer.writeBytes(ProtoData.varint2bytes(originLen));//压缩前长度
        writer.writeBytes(ProtoData.varint2bytes(compressLen));//压缩后长度
        writer.writeBytes(ToolBytes.hex2Bytes("\\x00\\x0d"));
        byte[] md5 = LocalCrypto.genMd5(
                ToolBytes.i(ToolBytes.BIG_ENDIAN).uint2bytes(ctx.getUin()),
                ecdhKey
        );
        md5 = LocalCrypto.genMd5(
                ToolBytes.i(ToolBytes.LITTLE_ENDIAN).int2bytes(data.length),
                ecdhKey, md5
        );
        long adler32 = ToolZip.getAdler32(ToolBytes.joinBytes(md5, data));
        writer.writeBytes(ProtoData.varint2bytes(adler32));
        writer.writeBytes(ToolBytes.hex2Bytes("\\x09\\x00"));
        byte[] header = writer.finish();
        header[1] = (byte) ((header.length << 2) | (compress ? 0x01 : 0x02));
        return ToolBytes.joinBytes(header, body);
    }

    public static BaseRequest newBaseRequest(WechatCtx ctx) {
        return newBaseRequest(ctx, null);
    }

    public static BaseRequest newBaseRequest(WechatCtx ctx, byte[] session) {
        BaseRequest obj = new BaseRequest();
        obj.sessionKey = session != null ? session : (ctx != null ? ctx.getSessionKey() : null);
        obj.uin = ctx.getUin();
        obj.deviceID = ctx.getDeviceIdBytes();
        obj.clientVersion = ctx.genClientVersion();
        obj.deviceType = ctx.genDeviceType().getBytes(WechatConst.CHARSET);
        obj.scene = 0;
        log.info(GsonUtil.GsonString(obj));
        return obj;
    }

    public static byte[] packSendMsgRequestNew(WechatCtx ctx, String wxid, String content, int type, String[] at) {
        MicroMsgRequestNew obj = new MicroMsgRequestNew();
        obj.toUserName = newSKBuiltinString_t(wxid);
        obj.content = content;
        obj.type = type;//消息类型：文字消息=1，名片=42
        obj.createTime = (int) (ToolDate.now() / 1000l);
        obj.clientMsgId = obj.createTime + (int) (System.nanoTime() % 0xffff);
        if (at != null && at.length > 0) {
            obj.msgSource = "<msgsource><atuserlist>" +
                    "<![CDATA[" + ToolStr.joinArr(at, ",") + "]]>" +
                    "</atuserlist></msgsource>";
        }
        SendMsgRequestNew obj2 = new SendMsgRequestNew();
        obj2.count = 1;
        obj2.list.add(obj);
        return pack(ctx, ApiCmd.CGI_SEND_MSG_NEW, obj2.build(), false);
    }

    public static SendMsgResponseNew unpackSendMsgResponseNew(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return SendMsgResponseNew.parse(buff);
    }

    public static byte[] packSnsPostRequest(WechatCtx ctx, String xml) {
        SnsPostRequest obj = new SnsPostRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.objectDesc = newSKBuiltinBuffer_t(xml.getBytes(WechatConst.CHARSET));
        obj.withUserListCount = 0;
        obj.privacy = 0;
        obj.syncFlag = 0;
        obj.clientId = ToolStr.MD5(xml + ToolDate.now());
        obj.postBGImgType = 0;
        obj.groupCount = 0;
        obj.objectSource = 0;
        obj.referId = 0l;
        obj.blackListCount = 0;
        obj.groupUserCount = 0;
        obj.snsPostOperationFields = new SnsPostOperationFields();
        obj.snsPostOperationFields.contactTagCount = 0;
        obj.snsPostOperationFields.tempUserCount = 1;
        obj.mediaInfoCount = 0;
        if (ctx.hasClientCheckDat()) {
            obj.clientCheckDat = newSKBuiltinBuffer_t(CHECK_DATA_BYTES);
        }
        return pack(ctx, ApiCmd.CGI_SNS_POST, obj.build(), false);
    }

    public static SnsPostResponse unpackSnsPostResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return SnsPostResponse.parse(buff);
    }

    public static byte[] packSnsCommentRequest(WechatCtx ctx, String id, String wxid, String content) {
        SnsCommentRequest obj = new SnsCommentRequest();
        int createTime = (int) (ToolDate.now() / 1000l);
        obj.baseRequest = newBaseRequest(ctx);
        obj.action = new SnsActionGroup();
        obj.action.id = new BigInteger(id, 10).longValue();
        obj.action.parentId = 0l;
        obj.action.currentAction = new SnsAction();
        obj.action.currentAction.fromUsername = ctx.getUsername();
        obj.action.currentAction.toUsername = wxid;
        obj.action.currentAction.type = content == null ? 1 : 2;
        obj.action.currentAction.source = 0;
        obj.action.currentAction.createTime = createTime;
        obj.action.currentAction.content = content == null ? "" : content;
        obj.action.currentAction.replyCommentId = 0;
        obj.action.currentAction.replyCommentId2 = 0l;
        obj.clientId = ToolStr.MD5(wxid + ToolDate.now());
        return pack(ctx, ApiCmd.CGI_SNS_COMMENT, obj.build(), false);
    }

    public static SnsCommentResponse unpackSnsCommentResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return SnsCommentResponse.parse(buff);
    }

    public static byte[] packSetChatRoomAnnouncementRequest(WechatCtx ctx, String chatroom, String announcement) {
        SetChatRoomAnnouncementRequest obj = new SetChatRoomAnnouncementRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.chatRoomName = chatroom;
        obj.announcement = announcement;
        obj.setAnnouncementFlag = 1;
        return pack(ctx, ApiCmd.CGI_SET_CHATROOM_ANNOUNCEMENT, obj.build(), false);
    }

    public static SetChatRoomAnnouncementResponse unpackSetChatRoomAnnouncementResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return SetChatRoomAnnouncementResponse.parse(buff);
    }

    public static byte[] packUploadMsgImgRequest(
            WechatCtx ctx, String wxid, String fileId, String fileKey,
            int imgSize, int midSize, int thumbSize, String aesKey, String fileMd5, int checkSum) {
        UploadMsgImgRequest obj = new UploadMsgImgRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.clientImgId = newSKBuiltinString_t(fileKey);
        obj.fromUserName = newSKBuiltinString_t(ctx.getUsername());
        obj.toUserName = newSKBuiltinString_t(wxid);
        obj.totalLen = imgSize;
        obj.startPos = 0;
        obj.dataLen = imgSize;
        obj.data = newSKBuiltinBuffer_t(new byte[0]);
        obj.msgType = 3;
        obj.netType = 1;
        obj.cdnBigImgUrl = "";
        obj.cdnMidImgUrl = fileId;
        obj.aesKey = aesKey;
        obj.encryptVer = 1;
        obj.cdnBigImgSize = 0;
        obj.cdnMidImgSize = midSize;
        obj.cdnThumbImgUrl = fileId;
        obj.cdnThumbImgSize = thumbSize;
        obj.cdnThumbImgHeight = 0;
        obj.cdnThumbImgWidth = 0;
        obj.cdnThumbAesKey = aesKey;
        obj.md5 = fileMd5;
        obj.crc32 = checkSum;
        obj.hitMd5 = 0;
        return pack(ctx, ApiCmd.CGI_UPLOAD_MSG_IMG, obj.build(), false);
    }
    public static VerifyUserResponse unpackVerifyUserResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return VerifyUserResponse.parse(buff);
    }
    public static byte[] packUploadMsgImgRequest(WechatCtx ctx, String wxid, String id, int pos, int len, byte[] data) {
        UploadMsgImgRequest obj = new UploadMsgImgRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.clientImgId = newSKBuiltinString_t(id);
        obj.fromUserName = newSKBuiltinString_t(ctx.getUsername());
        obj.toUserName = newSKBuiltinString_t(wxid);
        obj.totalLen = len;
        obj.startPos = pos;
        obj.dataLen = data.length;
        obj.data = newSKBuiltinBuffer_t(data);
        obj.msgType = 3;
        obj.compressType = 0;
        obj.netType = 1;
        obj.photoFrom = 3;
        obj.msgForwardType = 0;
        return pack(ctx, ApiCmd.CGI_UPLOAD_MSG_IMG, obj.build(), true);
    }

    public static UploadMsgImgResponse unpackUploadMsgImgResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return UploadMsgImgResponse.parse(buff);
    }

    public static byte[] packVerifyUserRequest(WechatCtx ctx, String v1, String v2, int opcode, int scene, String content) {
        return packVerifyUserRequest(ctx, v1, v2, opcode, scene, content, "", "", "");
    }


    public static byte[] packVerifyUserRequest(WechatCtx ctx, String v1, String v2, int opcode, int scene, String content, String chatRoomUserName, String sourceUserName, String sourceNickName) {
        VerifyUser obj1 = new VerifyUser();
        obj1.value = v1;
        obj1.verifyUserTicket = v2;
        obj1.antispamTicket = v2;
        obj1.friendFlag = 0;
        obj1.unknown8 = 0;
        obj1.chatRoomUserName = chatRoomUserName;
        obj1.sourceUserName = sourceUserName;//optional(6)--string
        obj1.sourceNickName = sourceNickName;//optional(7)--string
        VerifyUserRequest obj2 = new VerifyUserRequest();
        obj2.baseRequest = newBaseRequest(ctx);
        obj2.opcode = opcode;
        obj2.verifyUserListSize = 1;
        obj2.verifyUserList.add(obj1);
        obj2.verifyContent = content;
        obj2.sceneTag = 1;
        obj2.sceneData = ToolBytes.i(ToolBytes.LITTLE_ENDIAN).int2bytes(scene);
        return pack(ctx, ApiCmd.CGI_VERIFY_USER, obj2.build(), false);
    }

    public static byte[] packUploadVideoRequest(
            WechatCtx ctx, String wxid, String fileId, String fileKey,
            int videoSize, int playLength, int thumbLength, String aesKey,
            String fileMd5, String fileNewMd5, int checkSum) {
        UploadVideoRequest obj = new UploadVideoRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.clientMsgId = fileKey;
        obj.fromUserName = ctx.getUsername();
        obj.toUserName = wxid;
        obj.thumbTotalLen = thumbLength;
        obj.thumbStartPos = thumbLength;
        obj.thumbData = newSKBuiltinBuffer_t(new byte[0]);
        obj.videoTotalLen = videoSize;
        obj.videoStartPos = 0;
        obj.videoData = newSKBuiltinBuffer_t(new byte[0]);
        obj.playLength = playLength;
        obj.networkEnv = 1;
        obj.cdnVideoUrl = fileId;
        obj.aesKey = aesKey;
        obj.encryptVer = 1;
        obj.videoMd5 = fileMd5;
        obj.videoNewMd5 = fileNewMd5;
        obj.crc32 = checkSum;
        obj.hitMd5 = 0;
        return pack(ctx, ApiCmd.CGI_UPLOAD_VIDEO, obj.build(), false);
    }

    public static UploadVideoResponse unpackUploadVideoResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return UploadVideoResponse.parse(buff);
    }

    public static byte[] packSnsUploadRequest(WechatCtx ctx, String id, int pos, int len, byte[] data) {
        SnsUploadRequest obj = new SnsUploadRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.type = 2;
        obj.startPos = pos;
        obj.totalLen = len;
        obj.buffer = newSKBuiltinBuffer_t(data);
        obj.clientId = id;
        obj.photoFrom = 3;
        obj.netType = 1;
        return pack(ctx, ApiCmd.CGI_SNS_UPLOAD, obj.build(), true);
    }

    public static SnsUploadResponse unpackSnsUploadResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return SnsUploadResponse.parse(buff);
    }

    public static SKBuiltinBuffer_t newSKBuiltinBuffer_t(byte[] buffer) {
        SKBuiltinBuffer_t obj = new SKBuiltinBuffer_t();
        obj.len = buffer.length;
        obj.buffer = buffer;
        return obj;
    }

    public static SKBuiltinString_t newSKBuiltinString_t(String val) {
        SKBuiltinString_t obj = new SKBuiltinString_t();
        obj.str = val;
        return obj;
    }

    public static byte[] packNewSyncRequest(WechatCtx ctx) {
        NewSyncRequest obj = new NewSyncRequest();
        obj.oplog = new CmdList();
        obj.oplog.count = 0;
        obj.selector = 7;
        obj.keyBuf = newSKBuiltinBuffer_t(ctx.getSyncKey());
        obj.scene = 3;
        obj.deviceType = ctx.genDeviceType();
        obj.unknow6 = 1;
        return pack(ctx, ApiCmd.CGI_NEW_SYNC, obj.build(), false);
    }

    public static NewSyncResponse unpackNewSyncResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        if (buff.length > 0) {
            NewSyncResponse response = NewSyncResponse.parse(buff);
            if (response != null) {
                if (WechatConst.DEBUG && response.ret == 0) {
                    WechatDebug.log(ctx, "WechatUtil.unpackNewSyncResponse", "CMD_NEW_SYNC|buff=" + buff.length);
                    WechatDebug.log(ctx, "WechatUtil.unpackNewSyncResponse", "syncKey(OLD)=" + WechatDebug.bytes(ctx.getSyncKey()) + "," + "syncKey(NEW)=" + WechatDebug.bytes(response.keyBuf.buffer));
                    int cmdListSize = response.cmdList.list.size();
                    WechatDebug.log(ctx, "WechatUtil.unpackNewSyncResponse", "CMD_NEW_SYNC|cmdList=" + cmdListSize + "|buff=" + BASE64.encode(buff));
                    if (ctx.getSyncKey() != null) {
                        WechatDebug.log(ctx, "WechatUtil.unpackNewSyncResponse", "CMD_NEW_SYNC|cmdList=" + cmdListSize + "|syncKey(OLD)=" + BASE64.encode(ctx.getSyncKey()));
                    }
                    if (response.keyBuf.buffer != null) {
                        WechatDebug.log(ctx, "WechatUtil.unpackNewSyncResponse", "CMD_NEW_SYNC|cmdList=" + cmdListSize + "|syncKey(NEW)=" + BASE64.encode(response.keyBuf.buffer));
                    }
                    ctx.getUser().toBuilder().setCurrentsyncKey(ByteString.copyFrom(response.keyBuf.buffer));
                }
            }
            return response;
        } else {
            NewSyncResponse response = new NewSyncResponse();
            response.ret = ApiCmd.CODE_OFFLINE;
            return response;
        }
    }

    public static byte[] packGetCDNDnsRequest(WechatCtx ctx) {
        GetCDNDnsRequest obj = new GetCDNDnsRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.clientIP = "127.0.0.1";
        return pack(ctx, ApiCmd.CGI_GET_CDN_DNS, obj.build(), false);
    }


    public static byte[] packHeartBeatRequest(WechatCtx ctx) {
        HeartBeatRequest obj = new HeartBeatRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.timeStamp = (int) (ToolDate.now() / 1000);
        return pack(ctx, ApiCmd.CGI_HEART_BEAT, obj.build(), false);
    }

    public static HeartBeatResponse unpackHeartBeatResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return HeartBeatResponse.parse(buff);
    }

    public static byte[] packDelTalkRoomMemberRequest(WechatCtx ctx, String chatroom, String wxid) {
        DelMemberReq obj1 = new DelMemberReq();
        obj1.memberName = newSKBuiltinString_t(wxid);
        DelTalkRoomMemberRequest obj2 = new DelTalkRoomMemberRequest();
        obj2.baseRequest = newBaseRequest(ctx);
        obj2.memberCount = 1;
        obj2.memberList.add(obj1);
        obj2.talkRoomName = chatroom;
        obj2.scene = 0;
        return pack(ctx, ApiCmd.CGI_DEL_CHATROOM_MEMBER, obj2.build(), false);
    }

    public static DelTalkRoomMemberResponse unpackDelTalkRoomMemberResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return DelTalkRoomMemberResponse.parse(buff);
    }

    public static byte[] packSearchContactRequest(WechatCtx ctx, String wxid) {
        SearchContactRequest obj = new SearchContactRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.userName = newSKBuiltinString_t(wxid);
        obj.opCode = 0;
        obj.fromScene = 1;
        obj.searchScene = 3;
        return pack(ctx, ApiCmd.CGI_SEARCH_CONTACT, obj.build(), false);
    }

    public static SearchContactResponse unpackSearchContactResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return SearchContactResponse.parse(buff);
    }


    public static byte[] packFacingCreateChatRoomRequest(WechatCtx ctx, int opCode, String passWord, Float longitude, Float latitude) {
        FacingCreateChatRoomRequest obj = new FacingCreateChatRoomRequest();
        obj.baseRequest = newBaseRequest(ctx);
        obj.opCode = opCode;
        obj.passWord = passWord;
        obj.longitude = longitude;
        obj.latitude = latitude;
        obj.precision = 1;
        obj.gPSSource = 0;
        return pack(ctx, ApiCmd.CGI_FACING_CREATE_CHATROOM, obj.build(), false);
    }

    public static FacingCreateChatRoomResponse unpackFacingCreateChatRoomResponse(WechatCtx ctx, byte[] data) {
        byte[] buff = unpack(ctx, data);
        return FacingCreateChatRoomResponse.parse(buff);
    }
}
