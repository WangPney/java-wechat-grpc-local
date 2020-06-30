package com.wxipad.wechat;


import com.google.protobuf.InvalidProtocolBufferException;
import com.wxipad.local.WechatProto;
import com.wxipad.local.WechatUtil;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import com.wxipad.wechat.tools.model.WechatReturn;
import com.wxipad.wechat.tools.tool.ToolBytes;
import com.wxipad.wechat.tools.tool.ToolDate;
import com.wxipad.wechat.tools.tool.ToolStr;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;

import static com.wxipad.wechat.tools.uitls.WechatUtil.getMd5;

@Slf4j
public class WechatApi {
    private static final int IMAGE_PACK_SIZE = 0x8000;
    private static final ArrayList<WechatIns> wechatInsList = new ArrayList<>();
    private static WechatIns getins(String id) throws ApiException {
        synchronized (wechatInsList) {
            for (WechatIns ins : wechatInsList) {
                if (ins.id.equals(id)) {
                    return ins;
                }
            }
        }
        throw new ApiException("找不到微信实例");
    }

    //创建
    public static WechatApiMsg create(int type, Callback callback, WechatApiMsg apiMsg) throws InvalidProtocolBufferException {
        WechatIns ins = new WechatIns(type, callback, apiMsg);
        synchronized (wechatInsList) {
            wechatInsList.add(ins);
        }
        apiMsg.randomId(ins.id);
        return apiMsg;
    }

    /**
     * 取实例
     *
     * @param apiMsg
     * @return
     */
    public static WechatIns get(WechatApiMsg apiMsg) {
        return get(apiMsg.randomId);
    }

    /**
     * 取实例
     * @param id
     * @return
     */
    public static WechatIns get(String id) {
        WechatIns ins = null;
        try {
            ins = getins(id);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return ins;
    }

    /**
     * 发送消息
     * @param apiMsg
     * @param data
     */
    public static void sendMsg(WechatApiMsg apiMsg, Object data) {
        get(apiMsg).sendMsg(data);
    }

    /**
     * 设置微信返回数据
     * @param apiMsg
     * @param code 状态
     * @param msg 概要
     * @param data 内容
     * @param cmd 接口ID
     * @return
     */
    public static WechatReturn setWechatReturn(WechatApiMsg apiMsg, int code, String msg, Object data, int cmd) {
        WechatIns ins = get(apiMsg.randomId);
        long timeStamp = System.currentTimeMillis() / 1000;
        String wechatApiId = getMd5(apiMsg.account + timeStamp);
        Object datas = data;
        if (datas == null || datas.equals("")) {
            datas = GsonUtil.GsonString(ins.wechatApiMsg.get());
        }
        if (ins.wechatReturn.get() == null) {
            ins.wechatReturn.set(new WechatReturn()
                    .account(apiMsg.account)
                    .randomId(apiMsg.randomId)
                    .serverId(apiMsg.serverId)
                    .serverHost(apiMsg.serverIp + ":" + apiMsg.serverPort)
                    .serverPort(apiMsg.serverPort)
                    .serverIp(apiMsg.serverIp));
        }
        ins.wechatReturn.get().code(code)
                .msg(msg)
                .account(apiMsg.account)
                .timeStamp((int) timeStamp)
                .wechatApiId(wechatApiId)
                .randomId(apiMsg.randomId)
                .result(true)
                .cmd(cmd)
                .retdata(datas);
        return ins.wechatReturn.get();
    }

    /**
     * 判断是否存在
     * @param id
     * @return
     */
    public static boolean exist(String id) {
        synchronized (wechatInsList) {
            for (WechatIns ins : wechatInsList) {
                if (ins.id.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean dispose() {
        synchronized (wechatInsList) {
            Iterator<WechatIns> iterator = wechatInsList.iterator();
            while (iterator.hasNext()) {
                WechatIns ins = iterator.next();
                ins.dispose();
                iterator.remove();
            }
        }
        return false;
    }

    /**
     *
     * @param id
     * @return
     */
    public static boolean dispose(String id) {
        synchronized (wechatInsList) {
            Iterator<WechatIns> iterator = wechatInsList.iterator();
            while (iterator.hasNext()) {
                WechatIns ins = iterator.next();
                if (ins.id.equals(id)) {
                    ins.dispose();
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  初始化
     * @param id
     * @param name
     * @param account
     */
    public static void init(String id, String name, String account) {
        get(id).deviceName(name).init(account);
    }

    /**
     * 取设备ID
     * @param id
     * @param name
     */
    public static void deviceName(String id, String name) {
        get(id).deviceName(name);
    }

    /**
     * 检查响应
     * @param obj
     */
    private static void checkResponse(Object obj) {
        checkResponse(obj, false);
    }

    /**
     * 检查响应
     * @param obj
     * @param skip
     */
    private static void checkResponse(Object obj, boolean skip) {
        if (obj == null) {
            try {
                throw new ApiException("response null");
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取登录二维码
     * @param id
     * @return
     */
    public static WechatObj.LoginQrcode getLoginQrcode(String id) {
        WechatObj.LoginQrcode response = get(id).getLoginQrcode();
        checkResponse(response);
        return response;
    }

    /**
     * 检测扫码状态
     * @param id
     * @return
     */
    public static WechatObj.LoginQrcode checkLoginQrcode(String id) {
        WechatIns ins = get(id);
        WechatObj.LoginQrcode response = ins.checkLoginQrcode();
        checkResponse(response);
        return response;

    }

    /**
     * 发起登录
     * @param cmd
     * @param id
     * @return
     * @throws ApiException
     */
    public static boolean manualAuth(int cmd, String id) throws ApiException {
        try {
            if (cmd == 1111) {
                return get(id).doLoginQrcode();
            } else if (cmd == 2222) {
                return get(id).doUserLogin(get(id).userData.get());
            } else if (cmd == 3333) {
                return get(id).doA16Login(get(id).userA16.get());
            } else if (cmd == 4444) {
                return get(id).doQQLogin();
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
        return false;
    }

    /**
     * 断线重连
     * @param id
     * @return
     * @throws ApiException
     */
    public static boolean autoAuth(String id) throws ApiException {
        try {
            return get(id).doLoginAuto();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 获取自动登录数据（用来断线重连）
     * @param id
     * @return
     * @throws ApiException
     */
    public static AutoLoginPack getAutoLoginPack(String id) throws ApiException {
        try {
            WechatIns ins = get(id);
            WechatObj.AutoPack autoPack = ins.getAutoPack();
            AutoLoginPack pack = new AutoLoginPack();
            String json = WechatTool.gsonString(autoPack);
            pack.data = json.getBytes(WechatConst.CHARSET);
            pack.token = WechatTool.md5(pack.data);
            pack.data = WechatTool.compress(pack.data);//压缩
            pack.data = WechatTool.aesEncrypt(pack.token, pack.data);//加密
            return pack;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 赋值自动登录数据
     * @param id
     * @param token
     * @param data
     * @throws ApiException
     */
    public static void setAutoLoginPack(String id, String token, String data) throws ApiException {
        setAutoLoginPack(id, AutoLoginPack.create(token, data));
    }

    /**
     * 赋值自动登录数据
     * @param id
     * @param pack
     * @throws ApiException
     */
    public static void setAutoLoginPack(String id, AutoLoginPack pack) throws ApiException {
        try {
            WechatIns ins = get(id);
            byte[] buff = WechatTool.aesDecrypt(pack.token, pack.data);//解密
            byte[] data = WechatTool.decompress(buff);//解压
            String json = new String(data, WechatConst.CHARSET);
            WechatObj.AutoPack autoPack = WechatTool.gsonObj(json, WechatObj.AutoPack.class);
            ins.setAutoPack(autoPack);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 发消息
     * @param id
     * @param wxid
     * @param content
     * @return
     * @throws ApiException
     */
    public static boolean sendText(String id, String wxid, String content) throws ApiException {
        return sendText(id, wxid, content, null);
    }

    /**
     * 发消息
     * @param id
     * @param wxid
     * @param content
     * @param at
     * @return
     * @throws ApiException
     */
    public static boolean sendText(String id, String wxid, String content, String[] at) throws ApiException {
        try {
            if (WechatConst.LOCAL) {
                return get(id).shortConn().sendSendMsg(wxid, content, at).baseResponse.ret == 0;
            } else {
                return get(id).sendMicroMsg(wxid, content);
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 发送app消息
     * @param id
     * @param wxid
     * @param title         标题
     * @param content       内容
     * @param pointUrl      指向url
     * @param thumburl      缩略图url
     * @param at
     * @return
     * @throws ApiException
     */
    public static boolean sendAppMsg(String id, String wxid, String title, String content, String pointUrl, String thumburl, String[] at) throws ApiException {
        try {
            content = WechatUtil.getAppMsgXml(title, content, pointUrl, thumburl);
            return get(id).shortConn().sendAppMsg(wxid, content, at).baseResponse.ret == 0;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 发送名片消息
     * @param id
     * @param wxid
     * @param userName
     * @param nickName
     * @param province
     * @param city
     * @param sex
     * @param at
     * @return
     * @throws IOException
     */
    public static boolean sendWxUserCardMsg(String id, String wxid, String userName, String nickName, String province, String city, int sex, String[] at) throws IOException {
        String content = WechatUtil.getWxUserCardXml(userName, nickName, province, city, sex);
        return get(id).shortConn().sendWxUserCardMsg(wxid, content, at).baseResponse.ret == 0;
    }

    /**
     * 发图片消息
     * @param id
     * @param wxid
     * @param path
     * @return
     * @throws ApiException
     */
    public static boolean sendImage(String id, String wxid, String path) throws ApiException {
        if (WechatTool.existFile(path)) {
            return sendImage(id, wxid, WechatTool.readFile(path));
        }
        return false;
    }

    /**
     * 发图片消息
     * @param id
     * @param wxid
     * @param data
     * @return
     * @throws ApiException
     */
    public static boolean sendImage(String id, String wxid, byte[] data) throws ApiException {
        try {
            if (WechatConst.LOCAL) {
                WechatIns ins = get(id);
                if (data != null && data.length > 0) {
                    int start = 0, length = data.length;
                    String fileKey = ToolStr.MD5(ins.getUsername() + "-" + wxid) + "_" + ToolDate.now();
                    while (start < data.length) {
                        int end = Math.min(start + IMAGE_PACK_SIZE, data.length);
                        byte[] partData = ToolBytes.subBytes(data, start, end);
                        WechatProto.UploadMsgImgResponse response = ins.shortConn().sendSendImg(
                                wxid, fileKey, start, length, partData);
                        if (response.baseResponse.ret != 0) {
                            return false;
                        }
                        start = end;
                    }
                    return true;
                }
                return false;
            } else {
                return get(id).sendImageMsg(wxid, data);
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }


    /**
     * 发送 Cdn 图片
     * @param id
     * @param wxid
     * @param content
     * @return
     * @throws ApiException
     */
    public static boolean sendRecvImage(String id, String wxid, String content) throws ApiException {
        try {
            WechatIns ins = get(id);
            Document doc = WechatTool.getDocument(content);
            NodeList list = doc.getElementsByTagName("img");
            if (list.getLength() > 0) {
                NamedNodeMap attrs = list.item(0).getAttributes();
                String fileId = null, md5 = null, length = null, aesKey = null,
                        thbLength = null, thbHeight = null, thbWidth = null;
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    String name = attr.getNodeName();
                    String value = attr.getNodeValue();
                    if ("cdnmidimgurl".equalsIgnoreCase(name)) {
                        fileId = value;
                    } else if ("md5".equalsIgnoreCase(name)) {
                        md5 = value;
                    } else if ("length".equalsIgnoreCase(name)) {
                        length = value;
                    } else if ("aeskey".equalsIgnoreCase(name)) {
                        aesKey = value;
                    } else if ("cdnthumblength".equalsIgnoreCase(name)) {
                        thbLength = value;
                    } else if ("cdnthumbheight".equalsIgnoreCase(name)) {
                        thbHeight = value;
                    } else if ("cdnthumbwidth".equalsIgnoreCase(name)) {
                        thbWidth = value;
                    }
                }
                if (fileId != null && md5 != null && length != null && aesKey != null) {
                    String fileKey = ToolStr.randomMD5() + "_" + ToolDate.now();
                    int imgLength = ToolStr.parse(length, 0);
                    int thumbLength = ToolStr.parse(thbLength, 0);
                    int thumbHeight = ToolStr.parse(thbHeight, 0);
                    int thumbWidth = ToolStr.parse(thbWidth, 0);
                    if (WechatConst.LOCAL) {
                        WechatProto.UploadMsgImgResponse response = ins.shortConn().sendSendImg(
                                wxid, fileId, fileKey, imgLength, imgLength, imgLength, aesKey, md5, 0);
                        return response.baseResponse.ret == 0;
                    } else {
                        return get(id).sendImageMsg(wxid, fileId, imgLength, aesKey, md5,
                                thumbLength, thumbHeight, thumbWidth);
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 发送 Cdn 视频消息
     * @param id
     * @param wxid
     * @param content
     * @return
     * @throws ApiException
     */
    public static boolean sendRecvVideo(String id, String wxid, String content) throws ApiException {
        try {
            WechatIns ins = get(id);
            Document doc = WechatTool.getDocument(content);
            NodeList list = doc.getElementsByTagName("videomsg");
            if (list.getLength() > 0) {
                NamedNodeMap attrs = list.item(0).getAttributes();
                String fileId = null, md5 = null, newMd5 = null,
                        length1 = null, length2 = null, length3 = null, aesKey = null;
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    String name = attr.getNodeName();
                    String value = attr.getNodeValue();
                    if ("cdnvideourl".equalsIgnoreCase(name)) {
                        fileId = value;
                    } else if ("md5".equalsIgnoreCase(name)) {
                        md5 = value;
                    } else if ("newmd5".equalsIgnoreCase(name)) {
                        newMd5 = value;
                    } else if ("length".equalsIgnoreCase(name)) {
                        length1 = value;
                    } else if ("playlength".equalsIgnoreCase(name)) {
                        length2 = value;
                    } else if ("cdnthumblength".equalsIgnoreCase(name)) {
                        length3 = value;
                    } else if ("aeskey".equalsIgnoreCase(name)) {
                        aesKey = value;
                    }
                }
                if (fileId != null && md5 != null && newMd5 != null
                        && length1 != null && length2 != null && aesKey != null) {
                    String fileKey = ToolStr.randomMD5() + "_" + ToolDate.now();
                    int videoLength = ToolStr.parse(length1, 0);
                    int playLength = ToolStr.parse(length2, 0);
                    int thumbLength = ToolStr.parse(length3, 0);
                    if (WechatConst.LOCAL) {
                        WechatProto.UploadVideoResponse response = ins.shortConn().sendSendVideo(
                                wxid, fileId, fileKey, videoLength, playLength, thumbLength, aesKey, md5, newMd5, 0);
                        return response.baseResponse.ret == 0;
                    } else {
                        return get(id).sendVideoMsg(wxid, fileId, videoLength, playLength, thumbLength, aesKey, md5, newMd5);
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 朋友圈上传
     * @param id
     * @param path
     * @return
     * @throws ApiException
     */
    public static SnsUpload snsUpload(String id, String path) throws ApiException {
        if (WechatTool.existFile(path)) {
            return snsUpload(id, WechatTool.readFile(path));
        }
        return null;
    }

    /**
     * 朋友圈上传
     * @param id
     * @param data
     * @return
     * @throws ApiException
     */
    public static SnsUpload snsUpload(String id, byte[] data) throws ApiException {
        try {
            if (WechatConst.LOCAL) {
                WechatIns ins = get(id);
                String urlImage = null, urlThumb = null;
                int size = 0;
                if (data != null && data.length > 0) {
                    int start = 0, length = size = data.length;
                    String fileKey = ToolStr.MD5(ins.getUsername()) + "_" + ToolDate.now();
                    while (start < data.length) {
                        int end = Math.min(start + IMAGE_PACK_SIZE, data.length);
                        byte[] partData = ToolBytes.subBytes(data, start, end);
                        WechatProto.SnsUploadResponse response = ins.shortConn().sendSnsUpload(fileKey, start, length, partData);
                        if (response.baseResponse.ret != 0) {
                            return null;
                        } else {
                            if (response.bufferUrl != null) {
                                urlImage = response.bufferUrl.url;
                            }
                            if (response.thumbUrlCount != null && response.thumbUrlCount > 0
                                    && response.thumbUrls != null && !response.thumbUrls.isEmpty()) {
                                urlThumb = response.thumbUrls.get(0).url;
                            }
                        }
                        start = end;
                    }
                }
                SnsUpload upload = new SnsUpload();
                upload.urlImage = urlImage;
                upload.urlThumb = urlThumb;
                upload.size = size;
                return upload;
            } else {
                String url = get(id).snsUploadImage(data);
                checkResponse(url);
                SnsUpload upload = new SnsUpload();
                upload.urlImage = url;
                upload.urlThumb = url;
                upload.size = 0;
                return upload;
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 朋友圈发送Xml
     * @param id
     * @param xml
     * @return
     * @throws ApiException
     */
    public static SnsObj snsSendXml(String id, String xml) throws ApiException {
        try {
            String rtnXml = xml;
            if (WechatConst.LOCAL) {
                WechatProto.SnsPostResponse response = get(id).shortConn().sendSnsPost(xml);
                checkResponse(response);
                if (response.snsObject != null) {
                    if (response.snsObject.objectDesc != null) {
                        rtnXml = new String(response.snsObject.objectDesc.buffer, WechatConst.CHARSET);
                    }
                }
            } else {
                String response = get(id).snsPost(xml);
                checkResponse(response);
                rtnXml = response;
            }
            Document doc = WechatTool.getDocument(rtnXml);
            WechatApi.SnsObj obj = new WechatApi.SnsObj();
            NodeList list = doc.getElementsByTagName("TimelineObject");
            if (list.getLength() > 0) {
                NodeList children = list.item(0).getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    String name = child.getNodeName();
                    String value = child.getTextContent();
                    if ("id".equalsIgnoreCase(name)) {
                        obj.id = value;
                    } else if ("username".equalsIgnoreCase(name)) {
                        obj.userName = value;
                    } else if ("createTime".equalsIgnoreCase(name)) {
                        obj.createTime = WechatTool.parseInt(value, 0);
                    } else if ("contentDesc".equalsIgnoreCase(name)) {
                        obj.objectDesc = value;
                    }
                }
            }
            return obj;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 发朋友圈
     * @param id
     * @param content
     * @return
     * @throws ApiException
     */
    public static SnsObj snsSend(String id, String content) throws ApiException {
        int contentStyle = 2;
        String xml = "<TimelineObject>" +
                "<id><![CDATA[0]]></id>" +
                "<username><![CDATA[" + self(id) + "]]></username>" +
                "<createTime><![CDATA[0]]></createTime>" +
                "<contentDescShowType>0</contentDescShowType>" +
                "<contentDescScene>0</contentDescScene>" +
                "<private><![CDATA[0]]></private>" +
                "<contentDesc><![CDATA[" + content + "]]></contentDesc>" +
                "<contentattr><![CDATA[0]]></contentattr>" +
                "<sourceUserName></sourceUserName>" +
                "<sourceNickName></sourceNickName>" +
                "<statisticsData></statisticsData>" +
                "<weappInfo><appUserName></appUserName><pagePath></pagePath></weappInfo>" +
                "<canvasInfoXml></canvasInfoXml>" +
                "<ContentObject>" +
                "<contentStyle><![CDATA[" + contentStyle + "]]></contentStyle>" +
                "<contentSubStyle><![CDATA[0]]></contentSubStyle>" +
                "<title></title>" +
                "<description></description>" +
                "<contentUrl></contentUrl>" +
                "</ContentObject>" +
                "<actionInfo>" +
                "<appMsg>" +
                "<mediaTagName></mediaTagName>" +
                "<messageExt></messageExt>" +
                "<messageAction></messageAction>" +
                "</appMsg>" +
                "</actionInfo>" +
                "</TimelineObject>";
        return snsSendXml(id, xml);
    }

    /**
     * 发朋友圈
     * @param id
     * @param content
     * @param paths
     * @return
     * @throws ApiException
     */
    public static SnsObj snsSend(String id, String content, String[] paths) throws ApiException {
        StringBuilder sb = new StringBuilder();
        //媒体内容前缀
        long createTime = System.currentTimeMillis() / 1000;
        int contentStyle = 1;
        String contentDesc = content == null ? "" : ("<![CDATA[" + content + "]]>");
        String partPrefix = "<TimelineObject><id><![CDATA[0]]></id><username><![CDATA[" + self(id) + "]]></username><createTime><![CDATA[" + createTime + "]]></createTime><contentDescShowType>0</contentDescShowType><contentDescScene>0</contentDescScene><private><![CDATA[0]]></private><contentDesc>" + contentDesc + "</contentDesc><contentattr><![CDATA[0]]></contentattr><sourceUserName></sourceUserName><sourceNickName></sourceNickName><statisticsData></statisticsData><weappInfo><appUserName></appUserName><pagePath></pagePath></weappInfo><canvasInfoXml></canvasInfoXml><ContentObject><contentStyle><![CDATA[" + contentStyle + "]]></contentStyle><contentSubStyle><![CDATA[0]]></contentSubStyle><title></title><description></description><contentUrl></contentUrl><mediaList>";
        sb.append(partPrefix);
        //媒体内容前缀
        if (paths != null) {
            for (String path : paths) {
                BufferedImage image = WechatTool.openImage(path);
                if (image != null) {
                    SnsUpload upload = snsUpload(id, path);
                    if (upload != null) {
                        String partMedia = "<media><id><![CDATA[0]]></id><type><![CDATA[2]]></type><title></title><description></description><private><![CDATA[0]]></private><url type=\"1\"><![CDATA[" + upload.urlImage + "]]></url><thumb type=\"1\"><![CDATA[" + upload.urlThumb + "]]></thumb><size totalSize=\"" + upload.size + ".0\" width=\"" + image.getWidth() + ".0\" height=\"" + image.getHeight() + ".0\"></size></media>";
                        sb.append(partMedia);
                        delay(50);//延迟50毫秒
                    } else {
                        return null;
                    }
                }
            }
        }
        //媒体内容后缀
        sb.append("</mediaList></ContentObject><actionInfo><appMsg><mediaTagName></mediaTagName><messageExt></messageExt><messageAction></messageAction></appMsg></actionInfo><appInfo><id></id></appInfo><location poiClassifyId=\"\" poiName=\"\" poiAddress=\"\" poiClassifyType=\"0\" city=\"\"></location><publicUserName></publicUserName><streamvideo><streamvideourl></streamvideourl><streamvideothumburl></streamvideothumburl><streamvideoweburl></streamvideoweburl></streamvideo></TimelineObject>");
        return snsSendXml(id, sb.toString());
    }

    /**
     * 朋友圈评论
     * @param id
     * @param sns
     * @param wxid
     * @param content
     * @return
     * @throws ApiException
     */
    public static boolean snsComment(String id, String sns, String wxid, String content) throws ApiException {
        try {
            if (WechatConst.LOCAL) {
                return get(id).shortConn().sendSnsComment(sns, wxid, content).baseResponse.ret == 0;
            } else {
                get(id).snsComment(sns, wxid, content == null ? 1 : 2, content);
                return true;
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 接受用户请求
     * @param id
     * @param v1
     * @param v2
     * @return
     * @throws ApiException
     */
    public static boolean acceptUser(String id, String v1, String v2) throws ApiException {
        try {
            return get(id).addContact(v1, v2);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 添加好友
     * @param id
     * @param v1
     * @param v2
     * @param type
     * @param verify
     * @return
     * @throws ApiException
     */
    public static boolean addUser(String id, String v1, String v2, int type, String verify) throws ApiException {
        try {
            //type为添加来源，0-微信号搜索，1-QQ号搜索，3-微信号搜索，4-QQ好友，8-通过群聊，12-来自QQ好友，14-通过群聊
            if (WechatConst.LOCAL) {
                WechatProto.VerifyUserResponse response = get(id).shortConn().sendVerifyUser(v1, v2, 2, type, verify, "");
                checkResponse(response);
                return true;
            } else {
                return get(id).addContact(v1, v2, 2, type, verify);
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 搜索用户
     * @param id
     * @param wxid
     * @return
     * @throws ApiException
     */
    public static ContactSearch searchUser(String id, String wxid) throws ApiException {
        try {
            WechatObj.ContactInfo response = get(id).searchContact(wxid);
            checkResponse(response, true);
            ContactSearch rtn = new ContactSearch();
            rtn.userName = response.UserName;
            rtn.nickName = response.NickName;
            rtn.bigHead = response.BigHeadImgUrl;
            rtn.smallHead = response.SmallHeadImgUrl;
            rtn.sex = response.Sex;
            rtn.signature = response.Signature;
            rtn.country = null;
            rtn.province = response.Province;
            rtn.city = response.City;
            rtn.v1 = null;
            rtn.v2 = null;
            if (response.UserName != null
                    && response.UserName.startsWith("v1_")) {
                rtn.v1 = response.UserName;
            } else {
                rtn.userName = response.ExtInfo;
            }
            if (response.Ticket != null && response.Ticket.startsWith("v2_")) {
                rtn.v2 = response.Ticket;
            }
            return rtn;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 取二维码
     * @param id
     * @param wxid
     * @param style
     * @return
     * @throws ApiException
     */
    public static GetQrcode getQrcode(String id, String wxid, int style) throws ApiException {
        try {
            WechatObj.QrcodeInfo response = get(id).getQrcode(wxid);
            checkResponse(response);
            GetQrcode rtn = new GetQrcode();
            rtn.qrcode = Base64.getDecoder().decode(response.QrcodeBuf);
            return rtn;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 群群成员
     * @param id
     * @param chatroom
     * @return
     * @throws ApiException
     */
    public static ArrayList<GroupMember> getChatRoomMember(String id, String chatroom) throws ApiException {
        try {
            WechatObj.ChatroomInfo response = get(id).getChatroomInfo(chatroom);
            checkResponse(response, true);
            if (response.ChatroomUsername != null) {
                ArrayList<GroupMember> members = new ArrayList<>();
                if (response.MemberDetails != null) {
                    for (WechatObj.ChatroomMember memberInfo : response.MemberDetails) {
                        GroupMember member = new GroupMember();
                        member.userName = memberInfo.Username;
                        member.nickName = memberInfo.NickName;
                        member.displayName = memberInfo.DisplayName;
                        member.bigHeadImgUrl = memberInfo.BigHeadImgUrl;
                        member.smallHeadImgUrl = memberInfo.SmallHeadImgUrl;
                        member.inviteUser = memberInfo.InviterUserName;
                        members.add(member);
                    }
                }
                return members;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 删除群成员
     * @param id
     * @param chatroom
     * @param wxid
     * @return
     * @throws ApiException
     */
    public static boolean deleteChatRoomMember(String id, String chatroom, String wxid) throws ApiException {
        try {
            return get(id).delChatroomMember(chatroom, wxid);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 面对面建群
     *
     * @param id
     * @param passWord  四位数密码
     * @param longitude 经度(浮点数)
     * @param latitude  纬度(浮点数)
     * @return
     * @throws ApiException
     */
    public static boolean setChatRoomAnnouncement(String id, String passWord, Float longitude, Float latitude) throws ApiException {
        try {
            WechatProto.FacingCreateChatRoomResponse response = get(id).shortConn().sendFacingCreateChatRoom(passWord, longitude, latitude);
            checkResponse(response, true);
            return response.baseResponse.ret == 0;

        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 设置群公告
     * @param id
     * @param chatroom
     * @param announcement
     * @return
     * @throws ApiException
     */
    public static boolean setChatRoomAnnouncement(String id, String chatroom, String announcement) throws ApiException {
        try {
            if (WechatConst.LOCAL) {
                WechatProto.SetChatRoomAnnouncementResponse response = get(id).shortConn().setChatRoomAnnouncement(chatroom, announcement);
                checkResponse(response, true);
                return response.baseResponse.ret == 0;
            } else {
                return get(id).setChatRoomAnnouncement(chatroom, announcement);
            }
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     *
     * @param id
     * @return
     * @throws ApiException
     */
    public static String self(String id) throws ApiException {
        try {
            return get(id).getUserName();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 确保实例唯一
     * @param id
     * @return
     * @throws ApiException
     */
    public static boolean online(String id) throws ApiException {
        return online(id, 0l);
    }

    /**
     *
     * @param id
     * @param wait
     * @return
     * @throws ApiException
     */
    public static boolean online(String id, long wait) throws ApiException {
        try {
            WechatIns ins = get(id);
            if (wait > 0) {
                long start = System.currentTimeMillis();
                while (!ins.online()) {
                    long end = System.currentTimeMillis();
                    if (end - start > wait) {
                        break;
                    }
                    delay(100l);
                }
            }
            return ins.online();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * 退出
     * @param id
     * @return
     * @throws ApiException
     */
    public static boolean logout(String id) throws ApiException {
        try {
            return get(id).doLogout();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     *
     * @param ms
     */
    public static void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            WechatDebug.echo(e);
        }
    }

    public static class Callback extends WechatIns.Callback {
        private final int CACHE_SIZE = 500;
        private final LinkedList<String> CACHE_LIST = new LinkedList();

        @Override
        public void online(WechatIns ins) {

        }

        @Override
        public void offline(WechatIns ins) {

        }

        @Override
        public void syncMessage(WechatIns ins, WechatObj.Message msg) {
            if (skipMessage(ins, msg)) {
                return;//过滤同步重复的消息
            }
            long timestamp = msg.CreateTime * 1000l;
            String msgSource = msg.MsgSource;
            String content = msg.Content;
            String fromUser = msg.FromUserName;
            String toUser = msg.ToUserName;
            if (msg.MsgType == 1) {
                //文本消息
                if (ins.isSelf(fromUser)) {
                    //自己发送的消息
                    if (ins.isGroup(toUser)) {
                        //发送到组群
                        String atlist1 = solveAtList(msgSource);
                        String[] atlist2 = atlist1 != null ? atlist1.split(",") : null;
                        groupText(ins, toUser, fromUser, fromUser, timestamp, content, atlist2, true);
                    } else {
                        //发送到个人
                        privateText(ins, fromUser, toUser, timestamp, content, true);
                    }
                } else {
                    if (ins.isGroup(fromUser)) {
                        //组群的消息
                        int index = content.indexOf(":\n");
                        if (index >= 0) {
                            String fromGroup = fromUser;
                            fromUser = content.substring(0, index);
                            content = content.substring(index + 2);
                            String atlist1 = solveAtList(msgSource);
                            String[] atlist2 = atlist1 != null ? atlist1.split(",") : null;
                            groupText(ins, fromGroup, fromUser, toUser, timestamp, content, atlist2, false);
                        }
                    } else {
                        //个人的消息
                        privateText(ins, fromUser, toUser, timestamp, content, false);
                    }
                }
            } else if (msg.MsgType == 3) {
                //图片消息
                byte[] thumb = null;
                if (msg.ImgBuf != null && msg.ImgBuf instanceof String) {
                    thumb = Base64.getDecoder().decode((String) msg.ImgBuf);
                }
                if (ins.isSelf(fromUser)) {
                    //自己发送的消息
                    if (ins.isGroup(toUser)) {
                        //发送到组群
                        groupImage(ins, toUser, fromUser, fromUser, timestamp, content, thumb, true);
                    } else {
                        //发送到个人
                        privateImage(ins, fromUser, toUser, timestamp, content, thumb, true);
                    }
                } else {
                    if (ins.isGroup(fromUser)) {
                        //组群的消息
                        int index = content.indexOf(":\n");
                        if (index >= 0) {
                            String fromGroup = fromUser;
                            fromUser = content.substring(0, index);
                            content = content.substring(index + 2);
                            groupImage(ins, fromGroup, fromUser, toUser, timestamp, content, thumb, false);
                        }
                    } else {
                        //个人的消息
                        privateImage(ins, fromUser, toUser, timestamp, content, thumb, false);
                    }
                }
            } else if (msg.MsgType == 37) {
                //添加好友申请消息
                solveMessage37(ins, fromUser, toUser, timestamp, content);
            } else if (msg.MsgType == 43) {
                //视频消息
                if (ins.isSelf(fromUser)) {
                    //自己发送的消息
                    if (ins.isGroup(toUser)) {
                        //发送到组群
                        groupVideo(ins, toUser, fromUser, fromUser, timestamp, content, true);
                    } else {
                        //发送到个人
                        privateVideo(ins, fromUser, toUser, timestamp, content, true);
                    }
                } else {
                    if (ins.isGroup(fromUser)) {
                        //组群的消息
                        int index = content.indexOf(":\n");
                        if (index >= 0) {
                            String fromGroup = fromUser;
                            fromUser = content.substring(0, index);
                            content = content.substring(index + 2);
                            groupVideo(ins, fromGroup, fromUser, toUser, timestamp, content, false);
                        }
                    } else {
                        //个人的消息
                        privateVideo(ins, fromUser, toUser, timestamp, content, false);
                    }
                }
            } else if (msg.MsgType == 10000) {
                //系统消息
                solveMessage10000(ins, fromUser, toUser, timestamp, content);
            } else {
                otherMessage(ins, msg);
            }
        }

        protected void solveMessage37(WechatIns ins, String fromUser, String toUser, long timestamp, String content) {
            if ("fmessage".equals(fromUser)) {
                Document doc = WechatTool.getDocument(content);
                NodeList list = doc.getElementsByTagName("msg");
                if (list.getLength() > 0) {
                    String username1 = null, v1 = null, v2 = null,
                            remark = null, username2 = null;
                    NamedNodeMap attrs = list.item(0).getAttributes();
                    for (int i = 0; i < attrs.getLength(); i++) {
                        Node attr = attrs.item(i);
                        String name = attr.getNodeName();
                        String value = attr.getNodeValue();
                        if ("fromusername".equalsIgnoreCase(name)) {
                            username1 = value;
                        } else if ("encryptusername".equalsIgnoreCase(name)) {
                            v1 = value;
                        } else if ("ticket".equalsIgnoreCase(name)) {
                            v2 = value;
                        } else if ("content".equalsIgnoreCase(name)) {
                            remark = value;
                        } else if ("sourceusername".equalsIgnoreCase(name)) {
                            username2 = value;
                        }
                    }
                    if (username1 != null && v1 != null && v2 != null) {
                        friendAdd(ins, username1, v1, v2, timestamp, remark, username2);
                    }
                }
            }
        }

        protected void solveMessage10000(WechatIns ins, String fromUser, String toUser, long timestamp, String content) {
            if (!ins.isSelf(fromUser)) {
                if (ins.isGroup(fromUser)) {
                    //组群的消息
                    if (content.startsWith("你")) {
                        ArrayList<String> inviteNicknames = solveNicknames(content, "你邀请_加入了群聊", '_');
                        if (inviteNicknames != null) {
                            groupInvite(ins, fromUser, fromUser, toUser, timestamp, inviteNicknames.get(0));
                        }
                    } else {
                        ArrayList<String> inviteNicknames = solveNicknames(content, "_邀请_加入了群聊", '_');
                        if (inviteNicknames != null) {
                            groupInvite(ins, fromUser, fromUser, toUser, timestamp, inviteNicknames.get(1));
                        }
                    }
                } else {
                    //个人的消息
                }
            }
        }

        public void privateText(WechatIns ins, String from_user, String to_user, long timestamp, String content, boolean self) {
        }

        public void groupText(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, String[] atlist, boolean self) {
        }

        public void privateImage(WechatIns ins, String from_user, String to_user, long timestamp, String content, byte[] thumb, boolean self) {
        }

        public void groupImage(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, byte[] thumb, boolean self) {
        }

        public void privateVideo(WechatIns ins, String from_user, String to_user, long timestamp, String content, boolean self) {
        }

        public void groupVideo(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String content, boolean self) {
        }

        public void groupInvite(WechatIns ins, String from_group, String from_user, String to_user, long timestamp, String nick_name) {
        }

        public void friendAdd(WechatIns ins, String from_user, String v1, String v2, long timestamp, String remark, String source_user) {
        }

        public void otherMessage(WechatIns ins, WechatObj.Message msg) {
        }

        private boolean skipMessage(WechatIns ins, WechatObj.Message msg) {
            String md5 = WechatTool.bytesToHex(
                    WechatTool.md5(ins.id + "_" + msg.CreateTime + "_" + msg.Content));
            synchronized (CACHE_LIST) {
                if (CACHE_LIST.contains(md5)) {
                    return true;
                } else {
                    CACHE_LIST.addLast(md5);
                    while (CACHE_LIST.size() > CACHE_SIZE) {
                        CACHE_LIST.removeFirst();
                    }
                    return false;
                }
            }
        }

        private ArrayList<String> solveNicknames(String text, String format, char holder) {
            if (text == null || format == null) {
                return null;
            }
            ArrayList<String> nicknames = new ArrayList<String>();
            int pos = 0;
            for (char ch1 : format.toCharArray()) {
                if (pos >= text.length()) {
                    return null;
                }
                char ch2 = text.charAt(pos);
                if (ch1 == holder) {
                    //匹配昵称
                    if (ch2 != '"') {
                        return null;//昵称未匹配
                    }
                    int index = text.indexOf('"', pos + 1);
                    if (index <= 0) {
                        return null;//昵称未匹配
                    }
                    nicknames.add(text.substring(pos + 1, index));
                    pos = index + 1;
                } else {
                    //匹配字符
                    if (ch1 != ch2) {
                        return null;//字符不匹配
                    }
                    pos++;
                }
            }
            return nicknames;
        }

        private String solveAtList(String source) {
            String field = "atuserlist";
            if (source != null) {
                String prefix = "<" + field + ">";
                String suffix = "</" + field + ">";
                int index1 = source.indexOf(prefix);
                int index2 = source.indexOf(suffix);
                if (index1 >= 0 && index2 >= 0 && index2 > index1) {
                    String content = source.substring(index1 + prefix.length(), index2).trim();
                    String prefix2 = "<![CDATA[";
                    String suffix2 = "]]>";
                    if (content.startsWith(prefix2) && content.endsWith(suffix2)) {
                        return content.substring(prefix2.length(), content.length() - suffix2.length());
                    } else {
                        return content;
                    }
                }
            }
            return null;
        }

    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
        public ApiException(Throwable cause) {
            super(cause);
        }
    }

    public static class GetLoginQrcode {
        public byte[] qrcode;//二维码图片数据
    }

    public static class CheckLoginQrcode {
        public int status;//二维码状态，0-未扫码，1-已扫码，2-已确认，4-已取消
        public String nickName;//用户昵称
        public String headUrl;//用户头像
        public int expiry;//二维码有效期
    }

    public static class SnsUpload {
        public String urlImage;//大图片URL地址
        public String urlThumb;//小图片URL地址
        public int size;//上传图片大小
    }

    public static class SnsObj {
        public String id;//朋友圈ID
        public String userName;//相关用户wxid
        public int createTime;//朋友圈发布时间
        public String objectDesc;//朋友圈内容
    }

    public static class GetQrcode {
        public byte[] qrcode;//二维码图片数据
    }

    public static class ContactSearch {
        public String userName;//微信ID
        public String nickName;//成员昵称
        public String bigHead;//微信大头像
        public String smallHead;//微信小头像
        public int sex;//性别
        public String signature;//签名档
        public String country;//国
        public String province;//省
        public String city;//市
        public String v1;//添加好友v1信息
        public String v2;//添加好友v2信息
    }

    public static class GroupMember {
        public String userName;
        public String nickName;
        public String displayName;
        public String bigHeadImgUrl;
        public String smallHeadImgUrl;
        public String inviteUser;
    }

    public static class AutoLoginPack {
        public byte[] token;
        public byte[] data;
        public static AutoLoginPack create(String token, String data) {
            AutoLoginPack pack = new AutoLoginPack();
            pack.token = WechatTool.hexToBytes(token);
            pack.data = Base64.getDecoder().decode(data);
            return pack;
        }
        public String getToken() {
            return WechatTool.bytesToHex(token);
        }
        public String getData() {
            return Base64.getEncoder().encodeToString(data);
        }
    }

}
