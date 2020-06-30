package com.wxipad.wechat;

import com.wxipad.client.CalculationService;
import com.wxipad.client.GrpcClient;
import com.wxipad.local.WechatProto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.logging.Logger;

@Slf4j
public class WechatObj {
    private static final Logger log = Logger.getLogger(WechatObj.class.getName());

    /**
     * GRPC连接池配置类
     */
    public static class GrpcConfig {
        public int AppPort;
        public int ApiPort;
        public String AppServer;
        public String AppId;
        public String AppKey;
        public String AppToken;
        public String AppCert;
        public String ApiServer;
        public String ApiId;
        public String ApiKey;
        public String ApiToken;
        public String ApiCert;
        public boolean Local;
        public static void init(GrpcConfig obj) {
            log.info("初始化 [User] 实例");
            WechatConst.LOCAL = obj.Local;
            String appCert = WechatTool.emptyToNull(obj.AppCert);
            if (appCert != null) {
                appCert = System.getProperty("user.dir") + "/src/main/resources/" + appCert;
                appCert = appCert.replace('/', File.separatorChar);
                log.info("加载 [{}] 证书"+ appCert);
            }
            GrpcClient.init(WechatIns.GRPC_NAME_APP,
                    WechatTool.emptyToNull(obj.AppServer), obj.AppPort,
                    WechatTool.emptyToNull(obj.AppId), WechatTool.emptyToNull(obj.AppKey),
                    WechatTool.emptyToNull(obj.AppToken), appCert);
            String apiCert = WechatTool.emptyToNull(obj.ApiCert);
            if (apiCert != null) {
                apiCert = apiCert.replace('/', File.separatorChar);
            }
            GrpcClient.init(WechatIns.GRPC_NAME_API,
                    WechatTool.emptyToNull(obj.ApiServer), obj.ApiPort,
                    WechatTool.emptyToNull(obj.ApiId), WechatTool.emptyToNull(obj.ApiKey),
                    WechatTool.emptyToNull(obj.ApiToken), apiCert);
        }
        public static void dispose() {
            GrpcClient.dispose(WechatIns.GRPC_NAME_APP);
            GrpcClient.dispose(WechatIns.GRPC_NAME_API);
        }
    }
    /**
     * 自动登录相关信息类
     */
    public static class AutoPack {
        public int Type;
        public String LongServer;
        public String ShortServer;
        public String UserData;
    }
    /**
     * 扫码登录返回结果类
     */
    public static class LoginQrcode {
        public int CheckTime;
        public int Status;
        public String Username;
        public String Password;
        public String HeadImgUrl;
        public String Nickname;
        public int ExpiredTime;
        public String Uuid;
        public String ImgBuf;
        public String NotifyKey;
        public String RandomKey;
        public String ImgBufUrl;
    }

    /**
     * 回调信息消息类
     */
    @Data
    public static class Message {
        public long MsgId;
        public String FromUserName;//发送者
        public String ToUserName;//接受者
        public int MsgType;//消息类型
        public String Content;//内容
        public int Status;//状态
        public int ImgStatus;//1推送图片,2普通图片
        public Object ImgBuf;//图片
        public int CreateTime;//消息发送时间戳
        public String MsgSource;//消息来源
        public String PushContent;//推送
        public long NewMsgId;//新消息ID
    }


    public static class NewSyncResponse {
        public Integer ret;
        public WechatProto.CmdList cmdList;
        public Integer continueFlag;
        public WechatProto.SKBuiltinBuffer_t keyBuf;
        public Integer status;
        public Integer onlineVersion;
        public Integer utc;
    }

    /**
     * 获取的联系人信息
     */
    @Data
    public static class ContactInfo {
        public int MsgType;//消息ID
        public String UserName;//微信号，陌生人时为v1数据
        public String NickName;//昵称
        public String Signature;//签名
        public String SmallHeadImgUrl;//小头像
        public String BigHeadImgUrl;//大头像
        public String Province;//省份
        public String City;//城市
        public String Remark;//备注
        public String Alias;//签名
        public int Sex;//性别
        public int ContactType;//联系人类型
        public int VerifyFlag;//验证标志
        public String LabelLists;
        public String ChatRoomOwner;
        public String EncryptUsername;
        public String ExtInfo;//陌生人时为微信号
        public String ExtInfoExt;//签名
        public String Ticket;//陌生人时为v2数据
        public long ChatroomVersion;//验证标志

    }

    /**
     * 获取的二维码信息
     */
    public static class QrcodeInfo {
        public String QrcodeBuf;
        public String FoterWording;

    }

    /**
     * 获取的群信息
     */
    public static class ChatroomInfo {

        public String ChatroomUsername;
        public long ServerVersion;
        public ChatroomMember[] MemberDetails;

    }

    /**
     * 获取的群成员信息
     */
    public static class ChatroomMember {

        public String Username;
        public String NickName;
        public String DisplayName;
        public String BigHeadImgUrl;
        public String SmallHeadImgUrl;
        public int ChatroomMemberFlag;
        public String InviterUserName;

    }


    /**
     * 配置文件信息
     */
    public static class ConfigBean {
        public int serverport; //16421
        public boolean Local; //true
        public String AppServer; //grpc.wxipad.com
        public int AppPort; //12580,
        public String AppId; //v1_xukeoscar_CodeVip
        public String AppKey; //v2_7b3d44d2ce848751f2f9d27993d93471
        public String AppToken; //v3_651fc2c44e3a0aced535fa2e5f16dfc6
        public String AppCert; //ca.crt
        public String ApiServer; //grpc.wxipad.com
        public int ApiPort; //12590
        public String ApiId; //joycdma
        public String ApiKey; //84b7f5027200db1edb12ea2865d413c5
        public String ApiToken; //807b85a6d14c30aeb42dc8093e77a72d
        public String ApiCert; //null
    }





}
