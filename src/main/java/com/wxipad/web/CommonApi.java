package com.wxipad.web;

import com.alibaba.fastjson.JSON;
import com.wxipad.wechat.WechatApi;
import com.wxipad.wechat.WechatExample;
import com.wxipad.wechat.WechatTool;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import com.wxipad.wechat.tools.model.WechatReturn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
public class CommonApi {
    private static final CommonApi INSTANCE = new CommonApi();
    private static ServiceManager grpvcserver = ServiceManager.getInstance();

    public static CommonApi getInstance() {
        return INSTANCE;
    }

    public static WechatReturn execute(WechatApiMsg wechatApiMsg) throws Exception {
        wechatApiMsg.serverId(ApplicationRunnerImpl.serverId)
                .serverIp(ApplicationRunnerImpl.serverIp)
                .serverPort(ApplicationRunnerImpl.serverPort);
        WechatReturn wechatReturn = new WechatReturn();
        int cmd = wechatApiMsg.getCmd();
        if (cmd == 9999) {
        }
        String randomid = wechatApiMsg.getRandomId().toUpperCase();
        if (StringUtils.isEmpty(randomid)) {
            randomid = UUID.randomUUID().toString().toUpperCase();
            wechatApiMsg.randomId(randomid);
        }
        WechatExample service = grpvcserver.getServiceByRandomId(wechatApiMsg);
        if (cmd == 502 || cmd == 1111 || cmd == 2222 || cmd == 3333 || cmd == 702) {
            service = grpvcserver.createServiceForReLogin(wechatApiMsg);
            switch (cmd) {
                case ApiCmd.LOGIN_QR_GET:
                    //获取二维码
                    service.getQrcode(cmd);
                    break;
                case ApiCmd.LOGIN_ONE:
                    //一键上线
                    service.autoLogin();
                    break;
                case ApiCmd.LOGIN_A62:
                    //62登录
                    service.userLogin();
                    break;
                case ApiCmd.LOGIN_A16:
                    //A16登录
                    service.a16Login();
                    break;
                case ApiCmd.LOGIN_QR_RELOGIN:
                    // 二维码重新登陆
//                    service.qrReLogin();
                    break;
                default:
                    log.info("参数错误！cmd:[{}]", cmd);
                    break;
            }
            wechatReturn = service.getStater();
        } else if (service != null) {
//            service.setLastRequestWeChatApiMsg(wechatApiMsg);
            /*if (wechatApiMsg.getGrpcPayLoads() != null && wechatApiMsg.getGrpcPayLoads().length > 0) {
                grpcPayLoads = GsonUtil.GsonToBean(wechatApiMsg.getGrpcPayLoads().toString(), GrpcPayLoads.class);
            }else {
            }*/
//            service.resetReturn();
//            grpcPayLoads = JSON.parseObject(wechatApiMsg.getGrpcPayLoadStr(),GrpcPayLoads.class);

            try {
                if (cmd == 6666) {
                    wechatReturn = service.getStater();
                } else {

                    //这下面我没有细分匹配到cmd，这个是之前兼容dll的时候就这么写了，比较懒，没换，你可以自己换
                        switch (cmd) {
//                            case ApiCmd.CLEAR_CONTACT:
//                                //清粉
//                                service.clearContact(grpcPayLoads);
//                                break;
//                            case ApiCmd.SEND_MOMENT:
//                                //发朋友圈
//                                service.sendMoment(grpcPayLoads);
//                                break;
                            case ApiCmd.CGI_SEND_MSG_NEW:
                                //发送文本消息
                                WechatApi.sendText(randomid, "22787942781@chatroom","hello",new String[]{"wxid_tjgqkyqf2lvz22"});
                                break;

                            case ApiCmd.CMD_SEND_IMAGE_MSG:
                                WechatApi.sendRecvImage(randomid, "filehelper","<msg>\n" +
                                        "\t<img aeskey=\"f610731dc56505c9f5648ac3d7f6757f\" cdnhdheight=\"0\" cdnhdwidth=\"0\" cdnmidheight=\"0\" cdnmidimgurl=\"3053020100044c304a0201000204c1a63fd802032f56c10204eae5e77302045d3fed750425617570696d675f633465393433623262333631613238345f31353634343730363432393033020401051a020201000400\" cdnmidwidth=\"0\" cdnthumbaeskey=\"f610731dc56505c9f5648ac3d7f6757f\" cdnthumbheight=\"120\" cdnthumblength=\"3761\" cdnthumburl=\"3053020100044c304a0201000204c1a63fd802032f56c10204eae5e77302045d3fed750425617570696d675f633465393433623262333631613238345f31353634343730363432393033020401051a020201000400\" cdnthumbwidth=\"68\" encryver=\"0\" hevc_mid_size=\"45832\" length=\"122086\" md5=\"d6580975e469df903aaca8deceac644a\"/>\n" +
                                        "</msg>");
//                                WechatApi.sendImage(randomid, "filehelper", "C:\\Users\\hansg\\Pictures\\u=448461948,1893502695&fm=26&gp=0.jpg");
                                break;
                            case ApiCmd.CMD_SEND_VIDEO_MSG:
                                WechatApi.sendRecvVideo(randomid, "filehelper", "<msg>\n" +
                                        "\t<videomsg aeskey=\"23e9828873ffb5d31aa7cb91660d18d7\" cdnthumbaeskey=\"23e9828873ffb5d31aa7cb91660d18d7\" cdnthumbheight=\"512\" cdnthumblength=\"8897\" cdnthumburl=\"306c020100046530630201000204c1a63fd802032df7310204243583b702045d3fed56043e617570766964656f5f633465393433623262333631613238345f313536343437303631325f313531303131333030373139326531333933303135373331340204010400040201000400\" cdnthumbwidth=\"288\" cdnvideourl=\"306c020100046530630201000204c1a63fd802032df7310204243583b702045d3fed56043e617570766964656f5f633465393433623262333631613238345f313536343437303631325f313531303131333030373139326531333933303135373331340204010400040201000400\" fromusername=\"wxid_tjgqkyqf2lvz22\" isad=\"0\" length=\"1217961\" md5=\"52a42af398fbfbd15bea5d8ef33bb528\" newmd5=\"19ee544fec8656a69fe7d7da6172b557\" playlength=\"13\"/>\n" +
                                        "</msg>");
                                break;

                            case ApiCmd.SEND_MOMENT:
//                                WechatApi.snsSend(randomid, "helo wexin",new String[]{"C:\\Users\\hansg\\Pictures\\u=448461948,1893502695&fm=26&gp=0.jpg"});
                                WechatApi.snsSendXml(randomid, "<TimelineObject><id>13120222755479695453</id><username>wxid_7byv6nvjwqkj22</username><createTime>1564052433</createTime><contentDesc>阿道夫</contentDesc><contentDescShowType>0</contentDescShowType><contentDescScene>3</contentDescScene><private>0</private><sightFolded>0</sightFolded><showFlag>0</showFlag><appInfo><id></id><version></version><appName></appName><installUrl></installUrl><fromUrl></fromUrl><isForceUpdate>0</isForceUpdate></appInfo><sourceUserName></sourceUserName><sourceNickName></sourceNickName><statisticsData></statisticsData><statExtStr></statExtStr><ContentObject><contentStyle>1</contentStyle><title></title><description></description><mediaList><media><id>13120222756047171666</id><type>2</type><title></title><description>阿道夫</description><private>0</private><userData></userData><subType>0</subType><videoSize width=\\\"\\\" height=\\\"\\\"></videoSize><url type=\\\"1\\\" videomd5=\\\"\\\">http://mmsns.qpic.cn/mmsns/PiajxSqBRaEKyA6DPofaRzWYsQzKAdcxpD6tj2btw5GMuwPxvNOQHXTO26nBrddNY/0</url><thumb type=\\\"1\\\">http://mmsns.qpic.cn/mmsns/PiajxSqBRaEKyA6DPofaRzWYsQzKAdcxpD6tj2btw5GMuwPxvNOQHXTO26nBrddNY/0</thumb><size width=\\\"\\\" height=\\\"\\\" totalSize=\\\"0\\\"></size></media></mediaList><contentUrl></contentUrl></ContentObject><actionInfo><appMsg><messageAction></messageAction></appMsg></actionInfo><location poiClassifyId=\\\"\\\" poiName=\\\"\\\" poiAddress=\\\"\\\" poiClassifyType=\\\"0\\\" city=\\\"\\\"></location><publicUserName></publicUserName><streamvideo><streamvideourl></streamvideourl><streamvideothumburl></streamvideothumburl><streamvideoweburl></streamvideoweburl></streamvideo></TimelineObject>");
                                break;

                            case ApiCmd.BATCH_UPLOAD_PICTURE:
                                WechatApi.SnsUpload snsUpload = WechatApi.snsUpload(randomid, "C:\\Users\\hansg\\Pictures\\u=448461948,1893502695&fm=26&gp=0.jpg");
                                log.info(JSON.toJSONString(snsUpload));
                                break;

                            case ApiCmd.CMD_SEARCH_CONTACT:
                                WechatApi.ContactSearch contactSearch = WechatApi.searchUser(randomid, "ecofulishe");
                                log.info("搜索的用户：{}",JSON.toJSONString(contactSearch));
                                break;

                            case ApiCmd.ADD_USER_TASK:
                                WechatApi.ContactSearch result = WechatApi.searchUser(randomid, "hgf1641197217");
                                if (result != null && result.v1 != null && result.v2 != null) {
                                    WechatApi.addUser(randomid, result.v1, result.v2, 3,
                                            "Hello - " + WechatTool.bytesToHex(WechatTool.randomBytes(2)));
                                };
                                break;

                            case ApiCmd.CGI_FACING_CREATE_CHATROOM:
                                WechatApi.setChatRoomAnnouncement(randomid, "2355", 116.39f, 38.94f);
                                break;

                            case ApiCmd.ACCEPT_ADD_FRIEND:
                                WechatApi.acceptUser(randomid,"v1_bc1cedcf6583c18d8a21aab73c0596bd12acfa80683d1e774806a390e51efca75ce075a92588d32a58587f42fa01fa06@stranger","v2_ace18786f4d7f0b1a30118bb9e365e44b32ca7c17e93e46a188711d813c544de91dd9055ede224232e099ebba64101603ac0a53f537c47e182b082dc9372a4ad@stranger");
                                break;

                            case ApiCmd.CMD_GET_CHATROOM_MEMBER:
                                ArrayList<WechatApi.GroupMember> chatRoomMember = WechatApi.getChatRoomMember(randomid, "22787942781@chatroom");
                                log.info("获取群聊详情 ==> {}", JSON.toJSONString(chatRoomMember));
                                break;

                            case ApiCmd.SEND_WXUSER_CARD_MSG:
                                WechatApi.sendWxUserCardMsg(randomid, "filehelper", "gh_8e2798bc02f3", "网购优惠王", "浙江", "中国", 0, null);
                                break;

                            case ApiCmd.SEND_APP_CARD_MSG:
                                WechatApi.sendAppMsg(randomid, "filehelper", "测试发送", "好看看看看","https://youngmall-share2.youzibuy.com/wechat/detail_page?numIid=575648311285%26sign=b508d4de82b8846eef42e08394d06f8b%26uid=277125879%26redirect=2%26from=groupmessage","https://ss1.bdstatic.com/5aAHeD3nKgcUp2HgoI7O1ygwehsv/media/ch1000/png/pc215.png",null);
                                break;


//                            case ApiCmd.DELETE_FRIEND:
//                                //删除好友
//                                service.deleteUser(grpcPayLoads);
//                                break;
//                            case ApiCmd.MASS_MSG:
//                                //群发消息
//                                service.massMessage(grpcPayLoads);
//                                break;
//                            case ApiCmd.CREATE_CHATROOM:
//                                //建群
//                                service.createChatroom(grpcPayLoads);
//                                break;
//                            case ApiCmd.ADD_CHAT_ROOM_MEMBER:
//                                //添加群成员
//                                service.addChatRoomMember(grpcPayLoads);
//                                break;
//                            case ApiCmd.DELETE_CHAT_ROOM_MEMBER:
//                                //删除群成员
//                                service.deleteChatRoomMember(grpcPayLoads);
//                                break;
//                            case ApiCmd.SET_CHAT_ROOM_NAME:
//                                //设置群名
//                                service.setChatroomName(grpcPayLoads);
//                                break;
//                            case ApiCmd.SET_CHAT_ROOM_ANNOUNCEMENT:
//                                //设置群公告
//                                service.setChatroomAnnouncement(grpcPayLoads);
//                                break;
//                            case ApiCmd.SET_HEAD_IMAGE:
//                                //设置头像
//                                service.setHeadImageWithoutChange(grpcPayLoads);
//                                break;
//                            case ApiCmd.SET_WX_INFO:
//                                //修改用户信息
//                                service.setUserInfo(grpcPayLoads);
//                                break;
//                            case ApiCmd.LOGOUT:
//                                //下线
//                                service.logout();
//                                break;
//                            case ApiCmd.GET_PEOPLE_NEARBY:
//                                //附近的人
//                                service.getPeopleNearby(grpcPayLoads);
//                                break;
//                            case ApiCmd.QUIT_CHAT_ROOM:
//                                //退出群
//                                service.quitChatroom(grpcPayLoads);
//                                break;
//                            case ApiCmd.SET_USER_REMARK:
//                                //设置备注
//                                service.setUserRemark(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_OBJECT_COMMENT:
//                                //朋友圈评论
//                                service.snsObjectComment(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_OBJECT_UP:
//                                //朋友圈点赞
//                                service.snsObjectUp(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_OBJECT_DELETE:
//                                //朋友圈删除
//                                service.snsObjectDelete(grpcPayLoads);
//                                break;
//                            case ApiCmd.JOIN_CHAT_ROOM_FORM_CODE:
//                                //扫码进群
//                                service.joinChatRoomFormCode(grpcPayLoads);
//                                break;
//                            case 124:
//                                //自动通过群邀请
//                                break;
//                            case ApiCmd.AUTO_ACCEPT_USER:
//                                //自动通过好友请求
//                                service.autoAcceptUser(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_TIME_LINE:
//                                //获取朋友圈动态
//                                service.snsTimeline(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_USER_PAGE:
//                                //获取好友朋友圈
//                                service.snsUserPage(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_OBJECT_DETAIL:
//                                //获取朋友圈详情
//                                service.snsObjectDetail(grpcPayLoads);
//                                break;
//                            case ApiCmd.GET_ROOM_QR_CODE:
//                                //获取群二维码
//                                service.getRoomQrcode(grpcPayLoads);
//                                break;
//                            case ApiCmd.ADD_USER_TASK:
//                                //搜号加人
//                                service.addUserTask(grpcPayLoads);
//                                break;
//                            case ApiCmd.SHAKE_GET:
//                                // 摇一摇
//                                service.shakeGet(grpcPayLoads);
//                                break;
//                            case ApiCmd.RESET_PASSWORD:
//                                //修改密码
//                                service.resetPassword(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_OBJECT_CANCEL:
//                                // 取消点赞
//                                service.snsObjectCancel(grpcPayLoads);
//                                break;
//                            case ApiCmd.SNS_OBJECT_COMMENT_DELETE:
//                                // 删除评论
//                                service.snsObjectCommentDelete(grpcPayLoads);
//                                break;
//                            case ApiCmd.DELETE_DEVICE:
//                                // 删除评论
//                                service.deleteDevice(grpcPayLoads);
//                                break;
//                            case ApiCmd.GET_CHAT_ROOM_DETAIL:
//                                // 获取群详情
//                                service.getChatroomDetail(grpcPayLoads);
//                                break;
//                            case ApiCmd.UPLOAD_MOBILE_CONTACT:
//                                // 上传通讯录
//                                service.uploadMobileContact(grpcPayLoads);
//                                break;
//                            case ApiCmd.ADD_MOBILE_CONTACT:
//                                // 通讯录加人
//                                service.addMobileContact(grpcPayLoads);
//                                break;
//                            case ApiCmd.MOD_USER_REMARK:
//                                // 修改好友备注
//                                service.modUserRemark(grpcPayLoads);
//                                break;
//                            case ApiCmd.SET_USE_NAME:
//                                // 设置微信号
//                                service.setUseName(grpcPayLoads);
//                                break;
//                            case ApiCmd.UPDATE_CONTACT:
//                                // 更新通讯录信息
//                                service.updateContact();
//                                break;
//                            case ApiCmd.FOCUS_PUBLIC:
//                                // 关注公众号
//                                service.focusPublic(grpcPayLoads);
//                                break;
//                            case ApiCmd.WEB_AUTH:
//                                // 网页授权登录
//                                service.webAuth(grpcPayLoads);
//                                break;
//                            case ApiCmd.WX_APPFOUCS:
//                                service.wxAppFoucs();
//                                break;
//                            case ApiCmd.CHANGE_GROUP:
//                                service.changeGroup(grpcPayLoads);
//                                break;
//                            case ApiCmd.READ_PUBLIC:
//                                // 阅读公众号
//                                service.readPublic(grpcPayLoads);
//                                break;
//                            case 992:
//
//                                break;
//                            case ApiCmd.GET_BALANCE_AND_BANK:
//                                // 获取余额和银行卡信息
//                                service.getObtainBalance();
//                                break;
//                            case ApiCmd.CONFIRM_TRANSFER:
//                                // 确认转账
//                                service.transfer(grpcPayLoads);
//                                break;
//                            case ApiCmd.APPLY_WITHDRAW:
//                                //提现
//                                service.applyWithDraw(grpcPayLoads);
//                                break;
//                            case ApiCmd.SEND_HONGBAO:
//                                // 发送红包（未完成）
//                                service.sendHb(grpcPayLoads);
//                                break;
//                            case ApiCmd.ACCEPT_ADD_FRIEND:
//                                // 通过好友请求
//                                service.acceptAddFriendAsk(grpcPayLoads);
//                                break;
//                            case ApiCmd.APPROVE_GROUP_INVITE:
//                                // 同意进群邀请
//                                service.approveAddChat(grpcPayLoads);
//                                break;
//                            case ApiCmd.BATCH_UPLOAD_PICTURE:
//                                // 批量上传图片
//                                service.batchUploadPic(grpcPayLoads);
//                                break;
                            default:
                                log.info("参数错误！cmd:[{}]", cmd);
                                break;
                        }
//                    功能算是很全了，我这个代码，之前卖50W，记好了，你从现在开始，欠我50W了
                    wechatReturn = service.getStater();
                    wechatReturn = new WechatReturn();
                    wechatReturn.setMsg("success");

                }
            } catch (Exception e) {
//                log.error(ExceptionUtil.parseException(e).toString());
                wechatReturn = new WechatReturn();
                wechatReturn.setMsg("error:" + e.getMessage());
                wechatReturn.setCode(400);
            }
        } else {
            wechatReturn = new WechatReturn();
            wechatReturn.setMsg("设备不在线");
//            wechatReturn.setCode(Constant.RETURN_CODE_FAILED);
        }
        return wechatReturn;
    }

//    public static String getserver() {
//        return confingBean.getExtranetId();
//    }

//    public static WechatMsg convert(UtilMsg msg) {
//        WechatMsg.Builder chatBuilder = WechatMsg.newBuilder();
//        BaseMsg.Builder baseBuilder = BaseMsg.newBuilder();
//        User.Builder userBuilder = User.newBuilder();
//        // 构建user对象
//        try {
//            userBuilder.setSessionKey(ByteString.copyFrom(msg.baseMsg.user.sessionKey));
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setDeviceId(msg.baseMsg.user.deviceId);
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setMaxSyncKey(ByteString.copyFrom(msg.baseMsg.user.maxSyncKey));
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setUin(msg.baseMsg.user.uin);
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setAutoAuthKey(ByteString.copyFrom(msg.baseMsg.user.autoAuthKey));
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setCookies(ByteString.copyFrom(msg.baseMsg.user.cookies));
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setCurrentsyncKey(ByteString.copyFrom(msg.baseMsg.user.currentsyncKey));
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setDeviceName(msg.baseMsg.user.deviceName);
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setDeviceType(msg.baseMsg.user.deviceType);
//        } catch (Exception e1) {
//        }
//        try {
//            userBuilder.setNickname(ByteString.copyFrom(msg.baseMsg.user.nickName, "utf-8"));
//            userBuilder.setUserExt(ByteString.copyFrom(msg.baseMsg.user.userExt, "utf-8"));
//        } catch (Exception e) {
//        }
//        try {
//            userBuilder.setUserame(msg.baseMsg.user.userName);
//        } catch (Exception e) {
//        }
//        // 构建basemsg对象
//        try {
//            baseBuilder.setCmd(msg.baseMsg.cmd);
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setCmdUrl(msg.baseMsg.cmdUrl);
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setLongHead(ByteString.copyFrom(msg.baseMsg.longHead));
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setLongHost(msg.baseMsg.longHost);
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setPayloads(ByteString.copyFrom(msg.baseMsg.payLoads));
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setPlayloadextend(ByteString.copyFrom(msg.baseMsg.playloadextend));
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setRet(msg.baseMsg.ret);
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setShortHost(msg.baseMsg.shortHost);
//        } catch (Exception e) {
//        }
//        try {
//            baseBuilder.setUser(userBuilder.build());
//        } catch (Exception e) {
//        }
//        // 构建msg对象
//        try {
//            chatBuilder.setToken(msg.token);
//        } catch (Exception e) {
//        }
//        try {
//            chatBuilder.setVersion(msg.version);
//        } catch (Exception e) {
//        }
//        try {
//            chatBuilder.setTimeStamp((int) msg.timeStamp);
//        } catch (Exception e) {
//        }
//        try {
//            chatBuilder.setIP(msg.ip);
//        } catch (Exception e) {
//        }
//        try {
//            chatBuilder.setBaseMsg(baseBuilder.build());
//        } catch (Exception e) {
//        }
//
//        return chatBuilder.build();
//    }
//
//    public static UtilMsg convert(WechatMsg msg) {
//        UtilMsg res = new UtilMsg();
//        res.ip = msg.getIP();
//        res.timeStamp = msg.getTimeStamp();
//        res.token = msg.getToken();
//        res.version = msg.getVersion();
//        res.baseMsg.cmd = msg.getBaseMsg().getCmd();
//        res.baseMsg.cmdUrl = msg.getBaseMsg().getCmdUrl();
//        res.baseMsg.longHead = msg.getBaseMsg().getLongHead().toByteArray();
//        res.baseMsg.longHost = msg.getBaseMsg().getLongHost();
//        res.baseMsg.payLoads = msg.getBaseMsg().getPayloads().toByteArray();
//        res.baseMsg.playloadextend = msg.getBaseMsg().getPlayloadextend().toByteArray();
//        res.baseMsg.ret = msg.getBaseMsg().getRet();
//        res.baseMsg.shortHost = msg.getBaseMsg().getShortHost();
//
//        res.baseMsg.user.autoAuthKey = msg.getBaseMsg().getUser().getAutoAuthKey().toByteArray();
//        res.baseMsg.user.cookies = msg.getBaseMsg().getUser().getCookies().toByteArray();
//        res.baseMsg.user.currentsyncKey = msg.getBaseMsg().getUser().getCurrentsyncKey().toByteArray();
//        res.baseMsg.user.deviceId = msg.getBaseMsg().getUser().getDeviceId();
//        res.baseMsg.user.deviceName = msg.getBaseMsg().getUser().getDeviceName();
//        res.baseMsg.user.deviceType = msg.getBaseMsg().getUser().getDeviceType();
//        res.baseMsg.user.maxSyncKey = msg.getBaseMsg().getUser().getMaxSyncKey().toByteArray();
//        res.baseMsg.user.nickName = msg.getBaseMsg().getUser().getNickname().toStringUtf8();
//        res.baseMsg.user.sessionKey = msg.getBaseMsg().getUser().getSessionKey().toByteArray();
//        res.baseMsg.user.uin = msg.getBaseMsg().getUser().getUin();
//        res.baseMsg.user.userExt = msg.getBaseMsg().getUser().getUserExt().toStringUtf8();
//        res.baseMsg.user.userName = msg.getBaseMsg().getUser().getUserame();
//
//        return res;
//    }

    public static class UtilMsg {
        public String token;
        public String version;
        public long timeStamp;
        public String ip;
        public UtilBase baseMsg = new UtilBase();

        public static class UtilBase {
            public int cmd;
            public String cmdUrl;
            public String longHost;
            public byte[] longHead;
            public byte[] payLoads;
            public UtilUser user = new UtilUser();
            public byte[] playloadextend;
            public int ret;
            public String shortHost;
        }

        public static class UtilUser implements Serializable {
            public byte[] sessionKey;
            public String deviceId;
            public byte[] maxSyncKey;
            public long uin;
            public byte[] autoAuthKey;
            public byte[] cookies;
            public byte[] currentsyncKey;
            public String deviceName;
            public String deviceType;
            public String nickName;
            public String userName;
            public String userExt;
        }
    }

//    public static String getServerMap(){
//        return JSON.toJSONString(grpvcserver.getLoginUsersMap());
//    }
//
//    public static Map<String, Object> getServerMapInfo(){
//        return grpvcserver.getLoginUsersMap();
//    }

}
