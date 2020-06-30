package com.wxipad.client;

/**
 * 功能描述 GRPC-JAVA层 服务端实现
 *
 * @author:
 * @date: 2019/7/27 002719:52
 */

import com.alibaba.fastjson.JSONObject;
import com.wxipad.proto.BaseMsg;
import com.wxipad.proto.User;
import com.wxipad.proto.WechatGrpc;
import com.wxipad.proto.WechatMsg;
import com.wxipad.web.ApiCmd;
import com.wxipad.wechat.WechatContext;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import static com.wxipad.wechat.tools.uitls.WechatUtil.hexStr2Str;

public class CalculationService {
    private static final Logger logger = Logger.getLogger(CalculationService.class.getName());
//    public static String CONFIG = System.getProperty("user.dir") + File.separator + "config.json";
    private Server server;

    /**
     * 启动服务
     *
     * @param port
     * @throws IOException
     */
    public void start(int port) throws IOException {
        server = ServerBuilder.forPort(port).addService(new BasicCalImpl()).build().start();
        logger.log(Level.INFO, "服务已经启动,监听端口：" + port);
//        String config = FileUtil.readString(CONFIG, "utf-8");
//        GrpcClient.Config obj = WechatTool.gsonObj(config, GrpcClient.Config.class);

        /*JSONObject jsonObject = JSON.parseObject(config);
        GrpcClient.Config obj = new GrpcClient.Config("127.0.0.1",12590,
                                                    jsonObject.getString("ApiKey"),
                                                    jsonObject.getString("ApiId"),
                                                    jsonObject.getString("AppCert"),
                                                    jsonObject.getString("ApiKey"));


        //String name, String ip, int port, String id, String key, String token, String cert
        GrpcClient.init("app", obj);*/
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.WARNING, "监听到JVM停止,正在关闭GRPC服务....");
            CalculationService.this.stop();
            logger.log(Level.WARNING, "服务已经停止...");
        }));

    }

    /**
     * 关闭服务
     */
    public void stop() {
        Optional.of(server).map(s -> s.shutdown()).orElse(null);
    }

    /**
     * 循环运行服务,封锁停止
     *
     * @throws InterruptedException
     */
    public void blockUnitShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * 实现的服务类
     */
    static class BasicCalImpl extends WechatGrpc.WechatImplBase {
        @Override
        public void helloWechat(WechatMsg request, StreamObserver<WechatMsg> responseObserver) {
            // 获取数据信息
            //你在这里写收到数据后到本地的方法和返回，那样你的前端就不需要改东西，只需要把之前的创建a16的方法启动起来就行
//            GrpcClient grpcClient = GrpcClient.get("app");
            // 比如说你在这里写那个方法的判断，cmd=653，返回的wechatMsg
            // 计算数据
//            WechatMsg wechatMsg = grpcClient.call(request, 5);

            BaseMsg.Builder baseMsg = BaseMsg.newBuilder(request.getBaseMsg());
            WechatMsg.Builder builder = request.toBuilder();
            BaseMsg.Builder newBaseMsg = null;
            User.Builder userBuilder = baseMsg.getUserBuilder();

            String deviceId = userBuilder.getDeviceId();
            // 将设备id适配
            if(deviceId.length() == 344) {
                deviceId = deviceId.substring(134, 198);
                deviceId = hexStr2Str(deviceId);
            }else if (deviceId.startsWith("@62@")){
                deviceId = deviceId.replace("@62@","");
            }
            userBuilder.setDeviceId(deviceId);
            baseMsg.setUser(userBuilder.build());

            switch (Math.abs(baseMsg.getCmd())){
                case ApiCmd.CGI_FACING_CREATE_CHATROOM:     {
                    newBaseMsg = builderFacingCreateChatRoom(baseMsg);
                }break;

                case ApiCmd.CGI_SEND_MSG_NEW:               {
                    newBaseMsg = builderSendMsg(baseMsg);
                }break;

                case ApiCmd.CGI_UPLOAD_MSG_IMG:             {
                    newBaseMsg = builderUploadImage(baseMsg);
                }break;

                case ApiCmd.CGI_SNS_POST:                   {
                    newBaseMsg = builderSnsPost(baseMsg);
                }break;

                case ApiCmd.CGI_SNS_COMMENT:                {
                    newBaseMsg = builderSnsComment(baseMsg);
                }break;

                case ApiCmd.CGI_UPLOAD_MSG_CDN_IMG:         {
                    newBaseMsg = builderUploadCdnImage(baseMsg);
                }break;

                case ApiCmd.CGI_UPLOAD_VIDEO:               {
                    newBaseMsg = builderUploadCdnVideo(baseMsg);
                }break;

                case ApiCmd.CGI_SNS_UPLOAD:                 {
                    newBaseMsg = builderSnsUpload(baseMsg);
                }break;

                case ApiCmd.CGI_ADD_USER:                   {
                    newBaseMsg = builderAddUser(baseMsg);
                }break;
                default:
                    break;
            }


            newBaseMsg.getUserBuilder().setDeviceId(request.getBaseMsg().getUser().getDeviceId());
            builder.setBaseMsg(newBaseMsg);
            request = builder.build();

            // 返回数据，完成此次请求
            responseObserver.onNext(request);
            responseObserver.onCompleted();
        }

        /**
         * 面对面建群组解包
         */
        public BaseMsg.Builder builderFacingCreateChatRoom(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                logger.info("jsonObject:" + jsonObject.toJSONString());
                return WechatContext.packFacingCreateChatRoomRequest(
                                    baseMsg.build(),
                                    jsonObject.getIntValue("opeCode"),
                                    jsonObject.getString("passWord"),
                                    jsonObject.getFloat("longitude"),
                                    jsonObject.getFloat("latitude"))
                                    .toBuilder();
            }else {
                return WechatContext.unpackFacingCreateChatRoomResponse(baseMsg.build()).toBuilder();
            }
        }

        /**
         * 发送普通消息组解包
         */
        public BaseMsg.Builder builderSendMsg(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packSendMsgRequestNew(baseMsg.build(),
                        jsonObject.getString("ToUserName"),
                        jsonObject.getString("Content"),
                        jsonObject.getIntValue("Type"),null).toBuilder();
            }else {
                return WechatContext.unpackSendMsgResponseNew(baseMsg.build(), null).toBuilder();
            }
        }

        public BaseMsg.Builder builderSnsPost(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packSendSnsPostRequest(baseMsg.build(), jsonObject.getString("Content")).toBuilder();
            }else {
                return WechatContext.unPackSnsPostResponse(baseMsg.build()).toBuilder();
            }
        }

        public BaseMsg.Builder builderSnsComment(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packSendSnsCommentRequest(baseMsg.build(),
                                                                jsonObject.getString("ID"),
                                                                jsonObject.getString("ToUsername"),
                                                                jsonObject.getString("Content")).toBuilder();
            }else {
                return WechatContext.unPackSendSnsCommentResponse(baseMsg.build()).toBuilder();
            }
        }

        public BaseMsg.Builder builderUploadCdnImage(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packUploadCdnMsgImgRequest(baseMsg.build(),
                        jsonObject.getString("ToUserName"),
                        jsonObject.getString("CDNMidImgUrl"),
                        jsonObject.getString("ClientImgId"),
                        jsonObject.getIntValue("TotalLen"),
                        jsonObject.getIntValue("CDNMidImgSize"),
                        jsonObject.getIntValue("CDNThumbImgSize"),
                        jsonObject.getString("AESKey"),
                        jsonObject.getString("MD5"),
                        0).toBuilder();
            }else {
                return WechatContext.unPackUploadCdnMsgImgResponse(baseMsg.build()).toBuilder();
            }
        }

        public BaseMsg.Builder builderUploadImage(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packUploadMsgImgRequest(baseMsg.build(),
                                    jsonObject.getString("ToUserName"),
                                    jsonObject.getString("ClientImgId"),
                                    jsonObject.getIntValue("StartPos"),
                                    jsonObject.getIntValue("TotalLen"),
                                    Base64.getDecoder().decode(jsonObject.getString("Data"))).toBuilder();
            }else {
                return WechatContext.unPackUploadCdnMsgImgResponse(baseMsg.build()).toBuilder();
            }
        }

        public BaseMsg.Builder builderSnsUpload(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packSnsUploadRequest(baseMsg.build(),
                                                    jsonObject.getString("ClientId"),
                                                    jsonObject.getIntValue("StartPos"),
                                                    jsonObject.getIntValue("TotalLen"),
                                                    Base64.getDecoder().decode(jsonObject.getString("Uploadbuf"))).toBuilder();
            }else {
                return WechatContext.unPackSnsUploadResponse(baseMsg.build()).toBuilder();
            }
        }

        public BaseMsg.Builder builderUploadCdnVideo(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packUploadCdnVideoRequest(baseMsg.build(),
                                                                jsonObject.getString("ToUserName"),
                                                                jsonObject.getString("ClientMsgId"),
                                                                jsonObject.getString("CDNVideoUrl"),
                                                                jsonObject.getIntValue("VideoTotalLen"),
                                                                jsonObject.getIntValue("PlayLength"),
                                                                jsonObject.getIntValue("ThumbTotalLen"),
                                                                jsonObject.getString("AESKey"),
                                                                jsonObject.getString("VideoMd5"),
                                                                jsonObject.getString("VideoNewMd5"),
                                                        0).toBuilder();
            }else {
                return WechatContext.unPackUploadCdnVideoResponse(baseMsg.build()).toBuilder();
            }
        }

        public BaseMsg.Builder builderAddUser(BaseMsg.Builder baseMsg){
            if (baseMsg.getCmd() > 0){
                String content = baseMsg.getPayloads().toStringUtf8();
                JSONObject jsonObject = JSONObject.parseObject(content);
                return WechatContext.packAddUserTask(baseMsg.build(),
                        jsonObject.getString("Encryptusername"),
                        jsonObject.getString("Ticket"),
                        2,
                        jsonObject.getIntValue("Sence"),
                        jsonObject.getString("Content"),
                        "",
                        "",
                        "").toBuilder();
            }else {
                return WechatContext.unPackUploadCdnVideoResponse(baseMsg.build()).toBuilder();
            }
        }


    }
}
