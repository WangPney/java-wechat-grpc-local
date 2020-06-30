package com.wxipad.wechat;

import com.google.protobuf.ByteString;
import com.wxipad.local.LocalMmtls;
import com.wxipad.local.WechatProto;
import com.wxipad.local.WechatUtil;
import com.wxipad.proto.BaseMsg;
import com.wxipad.proto.User;
import com.wxipad.web.ApiCmd;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;

import java.io.IOException;
import java.util.Optional;

import static com.wxipad.web.MyWebSocketServer.sendMessage;

public class WechatContext extends WechatCtx {

    public final BaseMsg msg;

    public WechatContext(BaseMsg msg) {
        this.msg = msg;
    }

    public static byte[] process(BaseMsg msg) {
        if (msg.getCmd() == ApiCmd.CGI_SEND_MSG_NEW) {
            //TODO:packSendMsgRequestNew
        } else if (msg.getCmd() == -ApiCmd.CGI_SEND_MSG_NEW) {
            //TODO:unpackSendMsgResponseNew
        } else if (msg.getCmd() == ApiCmd.CGI_UPLOAD_MSG_IMG) {
            //TODO:packUploadMsgImgRequest
        } else if (msg.getCmd() == -ApiCmd.CGI_UPLOAD_MSG_IMG) {
            //TODO:unpackUploadMsgImgResponse
        }
        return null;
    }

    /**
     * 发送消息封包
     * @param msg
     * @param wxid
     * @param content
     * @param type
     * @param at
     * @return
     */
    public static BaseMsg packSendMsgRequestNew(BaseMsg msg, String wxid, String content, int type, String[] at) {
        WechatContext context = new WechatContext(msg);
        BaseMsg.Builder builder = msg.toBuilder();

        byte[] bytes = WechatUtil.packSendMsgRequestNew(context, wxid, content, type, at);

        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/newsendmsg")
                .setLongHead(ByteString.copyFrom(LocalMmtls.makeLongPack(bytes, msg.getCmd())))
                .setRet(0).build();

    }

    /**
     * 发送消息返回解包
     * @param msg
     * @param data
     * @return
     */
    public static BaseMsg unpackSendMsgResponseNew(BaseMsg msg, byte[] data) {
        WechatContext context = new WechatContext(msg);
        BaseMsg.Builder builder = msg.toBuilder();

        WechatProto.SendMsgResponseNew response = WechatUtil.unpackSendMsgResponseNew(context, data == null ? msg.getPayloads().toByteArray() : data);
        byte[] bytes = response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
        builder.setPayloads(ByteString.copyFrom(bytes));
        builder.setRet(0);

        return builder.build();
    }

    /**
     * 发送朋友圈组包
     */
    public static BaseMsg packSendSnsPostRequest(BaseMsg msg, String xml){
        WechatContext context = new WechatContext(msg);
        BaseMsg.Builder builder = msg.toBuilder();

        byte[] bytes = WechatUtil.packSnsPostRequest(context, xml);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                        .setCmdUrl("/cgi-bin/micromsg-bin/mmsnspost")
                        .setRet(0)
                        .build();
    }

    /**
     * 发送朋友圈解包
     */
    public static BaseMsg unPackSnsPostResponse(BaseMsg msg){
        WechatContext context = new WechatContext(msg);
        BaseMsg.Builder builder = msg.toBuilder();

        WechatProto.SnsPostResponse response = WechatUtil.unpackSnsPostResponse(context, msg.getPayloads().toByteArray());
        byte[] bytes = response != null ? Optional.ofNullable(response).map(t -> t.snsObject).map(f -> f.objectDesc.buffer).orElse(null) : null;
        return builder.setPayloads(ByteString.copyFrom(bytes))
                    .setRet(0)
                    .build();
    }

    /**
     * 发送朋友圈评论组包
     */
    public static BaseMsg packSendSnsCommentRequest(BaseMsg msg, String snsId, String wxid, String content){
        WechatContext context = new WechatContext(msg);
        BaseMsg.Builder builder = msg.toBuilder();

        byte[] bytes = WechatUtil.packSnsCommentRequest(context, snsId, wxid, content);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/mmsnscomment")
                .setRet(0)
                .build();
    }

    /**
     * 发送朋友圈评论解包
     */
    public static BaseMsg unPackSendSnsCommentResponse(BaseMsg msg){
        WechatContext context = new WechatContext(msg);
        BaseMsg.Builder builder = msg.toBuilder();

        WechatProto.SnsCommentResponse response = WechatUtil.unpackSnsCommentResponse(context, msg.getPayloads().toByteArray());
        byte[] bytes = response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
        return builder.setPayloads(ByteString.copyFrom(bytes))
                        .setRet(0)
                        .build();
    }

    public static BaseMsg packUploadCdnMsgImgRequest(BaseMsg baseMsg, String wxid, String fileId, String fileKey, int imgSize, int midSize, int thumbSize, String aesKey, String fileMd5, int checkSum){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        byte[] bytes = WechatUtil.packUploadMsgImgRequest(context, wxid,fileId, fileKey, imgSize, midSize, thumbSize, aesKey, fileMd5, checkSum);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/uploadmsgimg")
                .setRet(0)
                .build();
    }

    public static BaseMsg unPackUploadCdnMsgImgResponse(BaseMsg baseMsg){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        WechatProto.UploadMsgImgResponse response = WechatUtil.unpackUploadMsgImgResponse(context, baseMsg.getPayloads().toByteArray());
        byte[] bytes = response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
        return builder.setPayloads(ByteString.copyFrom(bytes))
                        .setRet(0)
                        .build();
    }

    public static BaseMsg packUploadMsgImgRequest(BaseMsg baseMsg, String wxid, String id, int pos, int len, byte[] data){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        byte[] bytes = WechatUtil.packUploadMsgImgRequest(context, wxid, id, pos, len, data);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/uploadmsgimg")
                .setRet(0)
                .build();
    }

    public static BaseMsg packSnsUploadRequest(BaseMsg baseMsg, String id, int pos, int len, byte[] data){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        byte[] bytes = WechatUtil.packSnsUploadRequest(context, id, pos, len, data);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/mmsnsupload")
                .setRet(0)
                .build();
    }

    public static BaseMsg packUploadCdnVideoRequest(BaseMsg baseMsg, String wxid, String fileId, String fileKey, int videoSize, int playLength, int thumbLength, String aesKey,String fileMd5, String fileNewMd5, int checkSum){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        byte[] bytes = WechatUtil.packUploadVideoRequest(context, wxid, fileId, fileKey, videoSize, playLength, thumbLength, aesKey, fileMd5, fileNewMd5, checkSum);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/uploadvideo")
                .setRet(0)
                .build();
    }

    public static BaseMsg packAddUserTask(BaseMsg baseMsg, String v1, String v2, int opcode, int scene, String content, String chatRoomUserName, String sourceUserName, String sourceNickName){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        byte[] bytes = WechatUtil.packVerifyUserRequest(context, v1, v2, opcode, scene, content, chatRoomUserName, sourceUserName, sourceNickName);
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/verifyuser")
                .setRet(0)
                .build();
    }

    public static BaseMsg unPackAddUserTask(BaseMsg baseMsg){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        WechatProto.VerifyUserResponse response = WechatUtil.unpackVerifyUserResponse(context, baseMsg.getPayloads().toByteArray());
        byte[] bytes = response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setRet(0)
                .build();
    }

    public static BaseMsg unPackSnsUploadResponse(BaseMsg baseMsg) {
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        WechatProto.SnsUploadResponse response = WechatUtil.unpackSnsUploadResponse(context, baseMsg.getPayloads().toByteArray());
        byte[] bytes = response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setRet(0)
                .build();
    }

    public static BaseMsg unPackUploadCdnVideoResponse(BaseMsg baseMsg){
        WechatContext context = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        WechatProto.UploadVideoResponse response = WechatUtil.unpackUploadVideoResponse(context, baseMsg.getPayloads().toByteArray());
        byte[] bytes = response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
        return builder.setPayloads(ByteString.copyFrom(bytes))
                .setRet(0)
                .build();
    }


    /**
     * 同步消息封包
     * @param msg
     * @return
     */
    public static byte[] packNewSyncRequest(BaseMsg msg) {
        WechatContext context = new WechatContext(msg);
        return WechatUtil.packNewSyncRequest(context);
    }

    /**
     * 同步消息返回解包
     * @param msg
     * @param data
     * @return
     */
    public static byte[] unpackNewSyncResponse(BaseMsg msg, byte[] data) {
        WechatContext context = new WechatContext(msg);
        WechatProto.NewSyncResponse response = WechatUtil.unpackNewSyncResponse(context, data);
        return response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
    }

    /**
     * 同步消息返回解包
     *
     * @param msg
     * @return
     */
    public static BaseMsg unpackNewSyncResponse(BaseMsg msg) {
        BaseMsg.Builder builder = BaseMsg.newBuilder(msg);
        WechatContext context = new WechatContext(msg);
        WechatProto.NewSyncResponse response = WechatUtil.unpackNewSyncResponse(context, msg.getPayloads().toByteArray());
        builder.setPayloads(ByteString.copyFrom(WechatTool.gsonString(response).getBytes(WechatConst.CHARSET)));
        if (response.keyBuf != null && response.keyBuf.buffer != null){
            builder.getUser().toBuilder().setCurrentsyncKey(ByteString.copyFrom(response.keyBuf.buffer));
        }
        return builder.build();
    }

    /**
     * 上传MsgImg 封包
     * @param msg
     * @param wxid
     * @param id
     * @param pos
     * @param len
     * @param data
     * @return
     */
    /*public static byte[] packUploadMsgImgRequest(BaseMsg msg, String wxid, String id, int pos, int len, byte[] data) {
        WechatContext context = new WechatContext(msg);
        return WechatUtil.packUploadMsgImgRequest(context, wxid, id, pos, len, data);
    }*/

    /**
     * 上传MsgImg 返回解包
     * @param msg
     * @param data
     * @return
     */
    /*public static byte[] unpackUploadMsgImgResponse(BaseMsg msg, byte[] data) {
        WechatContext context = new WechatContext(msg);
        WechatProto.UploadMsgImgResponse response = WechatUtil.unpackUploadMsgImgResponse(context, data);
        return response != null ? WechatTool.gsonString(response).getBytes(WechatConst.CHARSET) : null;
    }*/

    /**
     * 上传面对面建群数据 封包
     * @param baseMsg
     * @param opCode
     * @param passWord
     * @param longitude
     * @param latitude
     * @return
     */
    public static BaseMsg packFacingCreateChatRoomRequest(BaseMsg baseMsg, int opCode, String passWord, Float longitude, Float latitude){
        WechatContext wechatContext = new WechatContext(baseMsg);
        BaseMsg.Builder builder = baseMsg.toBuilder();

        byte[] bytes = WechatUtil.packFacingCreateChatRoomRequest(wechatContext, opCode, passWord, longitude, latitude);
        builder.setPayloads(ByteString.copyFrom(bytes))
                .setCmdUrl("/cgi-bin/micromsg-bin/mmfacingcreatechatroom")
                .setLongHead(ByteString.copyFrom(LocalMmtls.makeLongPack(bytes,baseMsg.getCmd())))
                .setRet(0);

        return builder.build();
    }

    /**
     * 上传面对面建群数据解包
     * @param baseMsg
     * @return
     */
    public static BaseMsg unpackFacingCreateChatRoomResponse(BaseMsg baseMsg){
        BaseMsg.Builder builder = baseMsg.toBuilder();
        WechatContext context = new WechatContext(baseMsg);
        WechatProto.FacingCreateChatRoomResponse response = WechatUtil.unpackFacingCreateChatRoomResponse(context, baseMsg.getPayloads().toByteArray());
        builder.setPayloads(ByteString.copyFrom(WechatTool.gsonString(response).getBytes(WechatConst.CHARSET)));
        builder.setCmdUrl("/cgi-bin/micromsg-bin/mmfacingcreatechatroom");

        return builder.build();
    }

    /**
     * 获取用户数据
     *
     * @return
     */
    @Override
    public User getUser() {
        return msg.getUser();//返回会话的用户数据
    }


    /**
     * 撒送数据给前端用户
     * @param account
     */
    public void sendMsg(String account) {
        sendMsg(account, msg);
    }

    /**
     * 撒送数据给前端用户
     * @param account
     * @param data
     */
    public void sendMsg(String account, Object data) {
        String datas = GsonUtil.GsonString(data);
        if (data == null || data.equals("")) {
            datas = GsonUtil.GsonString(msg);
        }
        sendMsg(account, datas);
    }

    /**
     * 发送数据给前端用户
     * @param account
     * @param data
     */
    @Override
    public void sendMsg(String account, String data) {
        try {
            sendMessage(account, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean hasClientCheckDat() {
        return true;//使用iPad协议需要验证数据
    }

    @Override
    public int genClientVersion() {
        return 0x16060520;//固定iPad版本号
    }

    @Override
    public String genDeviceType() {
        return msg.getUser().getDeviceType();//使用会话的设备类型
    }

}
