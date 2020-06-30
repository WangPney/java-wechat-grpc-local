package com.wxipad.web;

/**
 * 功能描述
 *
 * @author: aweie
 * @date: 2019/7/6 000621:09
 */
public interface ApiCmd {
    int LOGIN_QR_GET = 502;
    int CGI_CHECK_LOGIN = 503;
    int CGI_QRCODE_LOGIN = 1111;
    int LOGIN_ONE = 702;
    int LOGIN_A16 = 3333;
    int LOGIN_A62 = 2222;
    int LOGIN_QR_RELOGIN = 4444;
    int GET_BALANCE_AND_BANK = 38572;
    int APPLY_WITHDRAW = 38575;
    int CONFIRM_TRANSFER = 3850;
    int CREATE_TRANSFER_key = 38583;
    int SEND_HONGBAO = 3851;
    int ACCEPT_ADD_FRIEND = 1000;
    int CLEAR_CONTACT = 104;
    int SEND_MOMENT = 105;
    int CGI_SEARCH_CONTACT = 106;
    int DELETE_FRIEND = 107;
    int MASS_MSG = 108;
    int CREATE_CHATROOM = 109;
    int CGI_UPLOAD_MSG_IMG = 110;
    int CGI_UPLOAD_MSG_CDN_IMG = 1101;
    int DELETE_CHAT_ROOM_MEMBER = 111;
    int SET_CHAT_ROOM_NAME = 112;
    int SET_CHAT_ROOM_ANNOUNCEMENT = 113;
    int SET_HEAD_IMAGE = 114;
    int SET_WX_INFO = 115;
    int LOGOUT = 116;
    int GET_PEOPLE_NEARBY = 117;
    int QUIT_CHAT_ROOM = 118;
    int SET_USER_REMARK = 119;
    int SNS_OBJECT_COMMENT = 120;
    int SNS_OBJECT_UP = 121;
    int SNS_OBJECT_DELETE = 122;
    int JOIN_CHAT_ROOM_FORM_CODE = 123;
    int AUTO_ACCEPT_USER = 125;
    int SNS_TIME_LINE = 126;
    int SNS_USER_PAGE = 127;
    int SNS_OBJECT_DETAIL = 128;
    int GET_ROOM_QR_CODE = 129;
    int ADD_USER_TASK = 130;
    int SHAKE_GET = 131;
    int RESET_PASSWORD = 132;
    int SNS_OBJECT_CANCEL = 133;
    int SNS_OBJECT_COMMENT_DELETE = 134;
    int DELETE_DEVICE = 135;
    int GET_CHAT_ROOM_DETAIL = 136;
    int UPLOAD_MOBILE_CONTACT = 137;
    int CGI_NEW_SYNC = 138;
    int MOD_USER_REMARK = 139;
    int SET_USE_NAME = 140;
    int UPDATE_CONTACT = 1002;
    int CGI_VERIFY_USER = 137;
    int WEB_AUTH = 238;
    int WX_APPFOUCS = 1029;
    int CHANGE_GROUP = 990;
    int READ_PUBLIC = 991;
    int MARK_FRIEND = 139;
    int APPROVE_GROUP_INVITE = 774;
    int BATCH_UPLOAD_PICTURE = 775;
    int CGI_FACING_CREATE_CHATROOM = 653;
    int CGI_GET_A8KEY = 233;
    int CGI_UPLOAD_VIDEO = 149;
    int CGI_DEL_CHATROOM_MEMBER = 179;
    int CGI_SNS_UPLOAD = 207;
    int CGI_GET_CDN_DNS = 379;
    int CGI_HEART_BEAT = 518;
    int CGI_SNS_POST = 209;
    int CGI_SNS_COMMENT = 213;
    int CGI_SEND_MSG_NEW = 522;
    int CGI_ADD_USER = 1137;
    int CGI_SET_CHATROOM_ANNOUNCEMENT = 993;
    int CODE_OFFLINE = -99999;
    int CMD_NEW_SYNC = 138;
    int CMD_GET_LOGIN_QRCODE = 502;
    int CMD_CHECK_LOGIN_QRCODE = 503;
    int CMD_DO_LOGIN_QRCODE = 1111;//二维码扫码登录
    int CMD_DO_USER_LOGIN = 2222;//62登录
    int CMD_DO_ANDROID_LOGIN = 3333;//安卓登录
    int CMD_DO_QQ_LOGIN = 4444;//QQ浏览器登录
    int CMD_DO_LOGIN_AUTO = 702;
    int CMD_HEARTBEAT = 205;
    int CMD_SEND_MIRCO_MSG = 522;
    int CMD_SEND_IMAGE_MSG = 110;
    int CMD_SEARCH_CONTACT = 106;
    int CMD_ADD_CONTACT = 137;
    int CMD_SEND_VIDEO_MSG = 149;
    int CMD_GET_QRCODE = 168;
    int CMD_GET_CHATROOM_MEMBER = 551;
    int CMD_DEL_CHATROOM_MEMBER = 179;
    int CMD_SET_CHATROOM_ANNOUNCEMENT = 993;
    int CMD_SNS_UPLOAD = 207;
    int CMD_SNS_POST = 209;
    int CMD_SNS_COMMENT = 213;
    int CMD_LOGOUT = 282;
    int SEND_WXUSER_CARD_MSG = 283;
    int SEND_APP_CARD_MSG = 284;
}


