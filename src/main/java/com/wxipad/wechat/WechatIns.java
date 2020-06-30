package com.wxipad.wechat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wxipad.client.GrpcClient;
import com.wxipad.client.WxClient;
import com.wxipad.local.LocalShort;
import com.wxipad.local.WechatProto;
import com.wxipad.proto.BaseMsg;
import com.wxipad.proto.User;
import com.wxipad.proto.WechatMsg;
import com.wxipad.web.ApiCmd;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import com.wxipad.wechat.tools.model.WechatReturn;
import com.wxipad.wechat.tools.uitls.WechatUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.wxipad.web.MyWebSocketServer.sendMessage;
import static com.wxipad.wechat.WechatApi.setWechatReturn;
import static com.wxipad.wechat.WechatContext.packNewSyncRequest;
import static com.wxipad.wechat.WechatContext.unpackNewSyncResponse;
import static com.wxipad.wechat.tools.uitls.WechatUtil.*;

public class WechatIns extends WechatCtx {
    private static final long TIMEOUT_LONG = 5 * 1000l;
    private static final int TIMEOUT_SHORT = 10 * 1000;
    private static final int HTTP_CODE_OK = 200;
    static final int TYPE_IPAD = 1;//iPad协议
    private static final int TYPE_IPHONE = 2;//iPhone 协议
    private static final int TYPE_IMAC = 3;//iMac协议
    private static final int TYPE_ANDROID = 4;//安卓 协议
    private static final int TYPE_WINDOWS = 5;//Windows 电脑版 协议
    private static final int TYPE_WINPHONE = 6;//Windows 手机版 协议
    private static final int TYPE_QQC = 7;//QQ浏览器协议
    private static final String SESSION_KEY = "507580550237B47E8D5DB9DC708E0F80";
    private static final String WX_SERVER_SHORT = "szshort.weixin.qq.com";
    private static final String WX_SERVER_LONG = "szlong.weixin.qq.com";
    private static final String VERSION_IPAD = "6.6.5.14";//iPad微信版本号
    private static final String VERSION_IMAC = "2.3.23";//iMac微信版本号
    private static final int VERSION_IPAD_060620 = 0x16060520;//iPad微信版本号-6.6.5.14
    public static final int VERSION_IPAD_070004 = 0x17000435;//iPad微信版本号-7.0.4
    private static final int VERSION_IMAC_020312 = 0x12031212;//iMac微信版本号-2.3.12
    static final String GRPC_NAME_API = "api";
    static final String GRPC_NAME_APP = "app";
    private static final int SEEDS_SIZE = 16;//特性种子长度
    private static final int GRPC_RETRY = 5;//默认重试次数
    //随机人名
    private static final String[] PERSON_NAMES = {"千渔", "小驭", "开娥", "思宏", "启然", "子鑫", "张今", "中文", "高炎", "永匀", "晨荣", "辉君", "宏泽", "明莲", "昊钟", "文鸿", "骏意", "泓超", "漾东", "仕松", "丽菱", "观桓", "悠君", "子兵", "医水", "之石", "丰隽", "亚林", "炅学", "梅宇", "同结", "华明", "汶洲", "悠梅", "昕腾", "才涛", "同丽", "依飞", "雨勇", "泓瑶", "彦贞", "红强", "皙萱", "建英", "福轩", "业丽", "佳芮", "梓怡", "学美", "盛平", "剑开", "芮仪", "泞惠", "张璞", "点丽", "思崴", "姿宇", "恩才", "青霏", "张愉", "维叶", "秀诚", "一宾", "相时", "昱哲", "采铭", "翌天", "海岳", "亮杰", "存原", "雁舒", "乐锋", "韶惠", "亚博", "旎婷", "梓远", "十锦", "丹祥", "季霖", "义涵", "雄月", "俊竣", "琼铭", "秋茵", "亦熠", "晨华", "俞麾", "琦娟", "瀚梅", "凡方", "皓仪", "春晶", "辰冉", "梓润", "蒋畅", "蒋贺", "君浩", "俊博", "小萱", "意辉", "蒋箐", "蒋锦", "迩谷", "蒋坤", "俊琪", "新红", "全领", "增佳", "蒋庸", "蒋斐", "开力", "文兴", "天彬", "士倩", "蒋克", "蒋婉", "锦明", "蒋晏", "泉良", "臻设", "程林", "咏爱", "鹏明", "耀儒", "韩博", "艺欣", "巨麟", "良珍", "虹胜", "潇宏", "安彤", "亦良", "润全", "立棠", "自芮", "乃盈", "蒋霓", "桂骏", "雅凤", "蒋斌", "亦鹏", "昀熙", "政玉", "蒋颜", "晓希", "宗丽", "浩钰", "小微", "蒋奕", "桢宸", "中今", "朝仪", "晓新", "子铄", "星舒", "子语", "怡晴", "春德", "蒋姗", "金煜", "帅雯", "贞元", "俏瞳", "丽铭", "蒋军", "春荷", "岩家", "蒋蕴", "梓睿", "晓彤", "雨萍", "涵涛", "佳雯", "雍宇", "佳嘉", "宝萌", "蒋迪", "蒋锌", "蒋坡", "蒋金", "广琪", "双明", "嘉葭", "泓杰", "蒋宁", "雨坤", "马芯", "钰林", "玉景", "春淇", "友成", "马梅", "马麟", "程辉", "荣平", "尉文", "丽杨", "晟玉", "怡菡", "凯诚", "誉红", "彦潼", "逸霞", "梓杭", "夕苗", "马煦", "思海", "米宵", "竞阳", "大羽", "爱冰", "亚茜", "克鑫", "一涵", "志玉", "瀛鑫", "树芝", "妸轩", "雨东", "凤帆", "颖亮", "上枫", "宏杰", "俊伟", "马蕾", "煜杰", "秦莹", "剑轩", "知亮", "宇婷", "煜元", "婧莲", "冲卫", "马之", "晚鹏", "鑫希", "静岚", "泊标", "中玲", "鹏艳", "恩华", "靖文", "马芳", "岽楷", "龙慧", "心紫", "思佳", "丹杰", "佳冰", "玳娜", "马了", "家涵", "应洪", "小欲", "秀治", "增琪", "马渔", "嘉实", "嗣华", "思颖", "永婷", "兴心", "马磬", "马媛", "修辰", "墨杰", "志雨", "洪思", "涪辉", "恩玲", "鸿泰", "梦玟", "马漩", "晓娟", "马淄", "向煌", "木文", "明杰", "景军", "紫君", "善晶", "房真", "昊松", "房微", "丁佳", "浩荃", "紫童", "杰舜", "良翔", "少芮", "建涵", "仁鸣", "房傧", "晓隽", "彤媛", "房祁", "福畅", "房亮", "翊鸽", "书宇", "淑泽", "房俊", "泰六", "思榕", "秀媚", "佳然", "誉霏", "伟海", "具军", "河涵", "小文", "竹芳", "雨聍", "怀荷", "彧明", "承彤", "睿淮", "冠义", "敏震", "宇涛", "兰坚", "筵明", "曾烁", "寰棉", "缜华", "世旎", "映林", "敬绩", "曾凝", "卓涵", "曾畅", "俊锋", "腊婷", "曾尚", "湛洋", "梅颖", "雅亮", "鸾凯", "雨梅", "曾城", "香波", "鸣佳", "香文", "悦薇", "文来", "智桥", "珍朵", "霆英", "天涵", "饴芹", "成红", "梓轩", "佳宇", "曾芙", "明帆", "子惠", "文芸", "青澄", "胜砚", "嘉旭", "曾超", "一迪", "曾莉", "梓渊", "昱博", "曾广", "锦鸣", "庆坤", "曾茸", "殷琨", "玉峰", "曾亮", "昶邑", "城涵", "梓含", "明莉", "秀一", "梦如", "彬晨", "云淼", "洪心", "哲钰", "鹏轩", "佳涛", "梦然", "冠樱", "驿轩", "雅媚", "曾瑜", "婉沁", "红源", "佳泽", "文馨", "嘉琳", "兆蕾", "晓健", "曾波", "睿燕", "曾原", "曾泺", "明浩", "曾彤", "曾钥", "雅岚", "凯勋", "曾乔", "韩爽", "瑞宇", "韩缘", "勐爱", "昌彬", "文鑫", "文尧", "雨舒", "祯智", "焯杰", "石泉", "则凡", "媛硕", "君升", "铭莹", "庄伟", "雨华", "韩婕", "尚珏", "韩钦", "彦荣", "小冠", "鑫齐", "惠聪", "韩于", "毓尔", "建平", "彬锛", "韩雨", "展恒", "韩烨", "晓明", "韩琼", "韩龙", "韩潼", "栩轩", "千宾", "怡龙", "韩七", "华弘", "韩童", "韩瑞", "丽豪", "韩旗", "韩晨", "韩维", "林谆", "宝宁", "新娇", "艺青", "韩梅", "丽丛", "昭亮", "桃恒", "永子", "韩妤", "学仁", "军霞", "韩静", "进锐", "嘉平", "秀清", "得林", "薇鸣", "梅渊", "庆怡", "佳升", "宏明", "鸣迪", "馨伦", "子瑶", "贵睿", "琼晨", "大民", "丽真", "希英", "彦武", "顺靖", "韩正", "羽辉", "呈恬", "沂涵", "立童", "闰乐", "敬淋", "妙绚", "叶耕", "瑾桓", "韩逸", "家宁", "凤仿", "瀚涛", "韩焱", "淇燮", "恩骏", "美翔", "泽江", "真谣", "郑萱", "傲云", "小涵", "郑跃", "红珊", "延明", "心云", "毛羲", "阔溪", "世涵", "茹芙", "龙怡", "春蕊", "思慧", "瑞华", "咏静", "锡达", "若入", "文宇", "虹婷", "作淇", "奕宾", "郑菁", "麒永", "永晗", "圣轩", "痍熹", "俊英", "媛瑾", "模彤", "法右", "建骋", "涵雅", "朝萍", "郑正", "娟苒", "斐龙", "城恩", "思予", "群弈", "鹏虎", "宜谦", "木智", "大修", "增澳", "郑菲", "锦萍", "思婷", "宝霞", "郑妤", "奕东", "绍明", "郑德", "滔菱", "正涵", "佳子", "智静", "怡适", "帅蕙", "郑迅", "小昊", "语师", "东瑶", "郑榕", "伟涵", "得宇", "东璇", "惠骏", "长璁", "郑柔", "晟娜", "艳豪", "郑彭", "三渝", "济平", "少几", "科生", "路昕", "郑赢", "羽茗", "沛硕", "郑恺", "艺池", "郑卉", "忠兰", "易晗", "玉馨", "占玲", "铖雨", "学英", "姚森", "楠蘅", "玉婷", "姚霭", "莉滨", "景诗", "福红", "姚柯", "小淼", "意瑞", "蓓平", "凯闻", "剑彤", "姚钺", "学燕", "应华", "国畅", "安烁", "晨臻", "荣富", "世文", "泽玉", "茂驿", "可瑜", "志捷", "姚盛", "奕哲", "洪轩", "姚婧", "曦伊", "全伟", "韵军", "咏哲", "蒙佟", "楚芳", "姚超", "姚元", "博众", "一琳", "青林", "跃甲", "姚乐", "园宇", "耀修", "银源", "铭元", "慧彦", "晋恬", "稚辉", "赫秦", "志哲", "明蓉", "睿龙", "月灏", "姚火", "鑫哲", "耀鹏", "银琪", "宛吉", "姚熙", "蔓洁", "思鹏", "松雯", "建雄", "沅祥", "姚宽", "庆芸", "云卿", "姚璇", "静晗", "楚茜", "柯诚", "锦玉", "姚流", "姚拓", "基宝", "姚薇", "子莹", "红萌", "澜梅", "惠平", "姚馨", "晟伟", "占耀", "姚婕", "歆雄", "佳翩", "明玲", "启晓", "柯辉", "姚含", "红怡", "忠淼", "春扬", "熠滨", "乔东", "弈威", "丹平", "明华", "铎婉", "芷民", "伍骏", "睿琳", "泠怡", "素磊", "语恒", "书辰", "杰杰", "亭涛", "溥林", "瀚飞", "玉然", "子松", "丽璐", "梦玲", "唐静", "贤雪", "一鑫", "唐文", "唐锋", "龙芳", "宛虹", "彤萍", "彦梅", "继梅", "唐上", "小玲", "擎伟", "伊蔓", "疏春", "唐桐", "晶平", "乃丽", "唐苗", "语曦", "奕发", "元星", "玉蒙", "思莹", "香丞", "祥泳", "舍文", "明辉", "琦乐", "秀品", "逸桔", "唐莫", "锦栓", "朔涵", "慕荣", "钻予", "庆伟", "延浩", "睿妍", "欣芝", "卫文", "子琪", "唐遥", "邵岩", "睿屏", "世华", "景娇", "骏根", "倩侠", "宗文", "乙薇", "静林", "唐星", "馨文", "嘉莹", "雯缓", "艳衡", "晓微", "婧杰", "涵新", "小波", "大榕", "笑福", "唐瑶", "小林", "金琴", "淳斌", "栩浩", "唐哲", "毅航", "名漪", "泽翔", "露瑜", "逸雯", "然彤", "泽凡", "姜兰", "为生", "博柳", "豪静", "姜煜", "胤智", "艺君", "建吉", "翔玲", "祉财", "筱辉", "军欣", "享祖", "翰生", "军雄", "纾明", "炫伟", "艺轩", "儒楠", "学涟", "姜励", "姜煊", "元铭", "康花", "德贤", "子鑫", "土洋", "冠腾", "建懿", "泽明", "宇基", "姿元", "保嘉", "强锋", "嘉珍", "姜奕", "晓源", "姜渝", "缦萌", "姜芳", "当甜", "人林", "垠琪", "佳茜", "秋菲", "紫淞", "惠翔", "建雅", "晓宁", "曾薇", "荣英", "姜鹂", "友岑", "竞岩", "丽坡", "姜芹", "京雨", "姜照", "国钦", "婀祁", "竞丽", "与煌", "姜烜", "希学", "七杰", "新柳", "尚聪", "荧平", "怡栩", "晨霖", "秭安", "姜淼", "宝睿", "宸源", "毛婷", "鸿晨", "姜砚", "璨芬", "政芬", "释媛", "嘉睫", "来立", "敦桦", "子健", "姜江", "悛洁", "庄敏", "艾兰", "瑞峰", "巍汶", "纪森", "振珂", "辛峰", "乐乜", "清童", "飞康", "家荣", "睿成", "思英", "和欣", "程焕", "骅文", "君琪", "辛凌", "昭妍", "旭文", "辛睿", "致婷", "思峰", "垄宇", "绘琛", "禾腾", "芷阳", "辛俊", "清予", "辛晔", "辛珏", "文华", "加鸢", "辛程", "嘉博", "嘉铭", "忠惠", "彦诺", "辛冬", "业栋", "晓涛", "显换", "小桦", "一佳", "曾心", "聿旺", "惠君", "麓桐", "齐棚", "齐远", "桂超", "胺晨", "晋硕", "雨轩", "翰平", "梓臻", "笑娴", "涵丰", "骢潍", "海风", "泽城", "雨军", "子平", "俊晔", "锦远", "靖心", "明萱", "亮萱", "汉轩", "凯涵", "法墨", "瑾奕", "淑铭", "晓源", "钰泫", "律如", "佑田", "齐千", "海燕", "子涵", "海阳", "曜茜", "艺君", "齐佳", "康聆", "诗珂", "朝东", "可洋", "泽呈", "齐昱", "雨鑫", "炳枫", "齐笛", "齐睫", "昌昙", "齐秀", "甘骊", "一毅", "思炜", "胤昊", "坤琪", "艾霖", "子铧", "千勤", "理洋", "齐帆", "齐婕", "紫诚", "洵宜", "丹荧", "阿熙", "若涓", "家雯", "齐晖", "正展", "雨翔", "宇朵", "媛建", "宇戈", "丰婷", "骏宸", "子祥", "文芹", "子胜", "玺帅", "明潭", "新甫", "千华", "慧震", "瑞茹", "星升", "若霞", "玉城", "健玲", "玉芸", "海夫", "鸿阳", "振婷", "诚涵", "思林", "水轩", "夏默", "嘉运", "聪烨", "雅庭", "澄林", "冬良", "巴菱", "叶林", "树亚", "海鹰", "慧霖", "太金", "雨晨", "橹涵", "可辛", "裕谨", "子慧", "勋怡", "泓馨", "惠茹", "明谊", "子芸", "江识", "葱阳", "夏薇", "一格", "云悦", "云玉", "新荣", "夏悠", "永木", "谈芳", "兰岳", "夏凡", "海昶", "乐霖", "田翔", "双方", "宇妮", "李彬", "予清", "建晨", "鸿林", "夏东", "相炅", "夏鲜", "夏寒", "瑗非", "夏与", "夏至", "文铭", "梓乾", "元素", "靖霖", "夏烨", "子平", "佳琳", "沛红", "夏果", "芷莉", "万蓓", "春豫", "煜滢", "钰舒", "黎月", "永强", "明润", "丞宇", "雯雅", "雁惠", "涵菱", "舒惠", "岚菱", "昊茗", "玥宁", "妘容", "芹潞", "从芸", "香琴", "芯茹", "冉抒", "茹轩", "蕊昱", "睿祁", "容歆", "轩玥", "芸绮", "盈雯", "凝遥", "萱焓", "多睿", "芩喧", "欣甜", "书乐", "颜抒", "昕宇", "谨谣", "锦然", "夏烁", "锦语", "玲鹭", "雪芩", "萧茹", "懿涵", "旭芹", "茵云", "羽溪", "忻灿", "珺谣", "馨元", "淑云", "莉馨", "梦蝶", "琳潼", "馨雅", "致语", "初露", "昕潞", "爱琴", "亦芸", "天翼", "昕芳", "依笛", "凤娟", "宇煊", "安娜", "惠彤", "娴易", "誉雯", "芩煊", "予宇", "婷秀", "雨彤", "夏颢", "雨泽", "婧云", "昱茹", "锦文", "荣诗", "笑芝", "语莲", "晓凡", "寄耘", "晗晨", "翠玲", "颜晗", "佳泓", "忻菲", "念暄", "琦兴", "秒欣", "颢舒", "玥暮", "容琼", "宸谣", "雁红", "耘雨", "盈昕", "丽书", "灵秀", "誉然", "湘晗", "雪甜", "文智", "姝伶", "芯洁", "译羽", "晨烨", "佩芩", "嘉羽", "语蔚", "美菏", "恩瑶", "纾芹", "芹蕊", "雁函", "涵妍", "彩菱", "聪晨", "诗煊", "玥函", "雯心", "含儒", "碧蝶", "莉语", "耘怀", "笑晗", "遥琴", "慧云", "函依", "南蓉", "清心", "遥羿", "羽思", "湘峪", "晨湘", "美儒", "兰韵", "念滢", "谣捷", "洳初", "瑾雯", "函文", "琴琀", "耘妤", "昕谣", "正妍", "舒睿", "修哲", "薇莹", "馨愉", "梦颜", "欢柔", "舒瑛", "云霏", "笑诗", "灵钰", "妍瑗", "含碹", "若辰", "宜岚", "璐雯", "毓静", "嘉美", "羽珏", "妍弘", "云忆", "雁鸿", "妍美", "纯娴", "艺含", "寄香", "蕊丹", "莺文", "芸璐", "笛妤", "彤夕", "晖莹", "清婉", "菲悦", "淑卉", "妘灏", "钧遥", "琳露", "芯文", "新睿", "聆雯", "依书", "锦颖", "以芸", "水芸", "雯亿", "润银", "舒萌", "婿睛", "旻琳", "翌茹", "莹翌", "如凡", "舒颐", "芳琳", "逸甜", "函芸", "歆洳", "暄笑", "翠茹", "宇欣", "抒谣", "淑琲", "雅姝", "颢轩", "安慧", "婌怀", "芹昊", "灵瞳", "涵燕", "宇宣", "秋莲", "锦雯", "智欣", "妤芠", "云纾", "烁浠", "珺谣", "琀雅", "韵颔", "水蓉", "意婌", "云含", "憧瑶", "依蝶", "雨译", "问雪", "晴晔", "辰茹", "婉曼", "羽轩", "晨悦", "雯彤", "昕叶", "采喧", "胜怡", "美瑗", "秀逸", "淑旋", "颍菡", "柔芹", "书枫", "婌莺", "鸿耘", "琳焓", "泽佳", "旭甜", "夕谣", "雅飞", "梓忻", "舒萌", "丽琳", "宛茹", "馨淯", "歆年", "蕊婷", "梦露", "莎雅", "函容", "千奕", "萱滢", "姝琦", "予羽", "舒弘", "雯予", "萤含", "诗姝", "欣斐", "茹渲", "露雅", "烨雪", "睿媚", "梦依", "黛萱", "莹骄", "欣辰", "雯歆", "馨琦", "涵韵", "安萱", "依笛", "心蕊", "舒菲", "鹭智", "姝逍", "琳善", "畅莺", "皓云", "焓音", "忆忻", "妙晴", "滢昱", "汝熙", "谣旭", "美羽", "舒晴", "若菡", "娜宇", "舒倩", "妘欷", "旻湘", "霜宇", "澜莹", "含湘", "颍舒", "誉珍", "霏涵", "函娇", "骄扬", "亿歆", "宁耘", "宇默", "亦蕊", "蕊誉", "晨洳", "语文", "佳韵", "欣聪", "柔如", "宁云", "加怡", "新言", "紫函", "心皓", "莉姝", "雨颢", "晟茹", "娴依", "含芹", "西函", "昕宜", "暄文", "羽意", "修含", "欣叶", "海洁", "滢倩", "莹雅", "晓纾", "若瑶", "云儒", "素忻", "湘孺", "书岚", "予云", "新凡", "梦懿", "茹琴", "书琀", "程歆", "焓玲", "羽姻", "亦彤", "之歆", "雨歆", "晗抒", "孺遥", "惜香", "语芩", "诗桐", "芩柔", "琳淼", "琴诺", "谦滢", "婌育", "妘勤", "奕浛", "秋荣", "舒桦", "儒梦", "易晨", "郁琬", "利羽", "翌焓", "雨娅", "宇雯", "溢琼", "羽莲", "誉然", "诗娜", "昊文", "玥姻", "潇睿", "舒文", "涵颖", "雨蓓", "睿晴", "小阳", "茹晗", "宁玲", "琴晔", "如雪", "慈遥", "馨梅", "向璐", "言烨", "芯憧", "昔纯", "梦芩", "誉纯", "耘绮", "昊汝", "晗颜", "可忆", "骊颖", "宸蕾", "语濡", "婌荣", "琴萱", "瑾琳", "淑柔", "菡露", "彦忻", "馨煊", "睿喧", "玥聪", "亦雯", "莹妃", "羽漩", "欣昱", "懿昕", "抒菲", "语灏", "溪雯", "馨毓", "蔓遥", "新婌", "心璐", "耘琪", "婷俪", "品梦", "昔琳", "语霏", "洁文", "晴含", "颢琴", "宸聪", "意耘", "莹弈", "容蕊", "姝羽", "悦亦", "汝元", "媛祺", "初鹭", "箫芩", "艺颖", "语真", "美扬", "思宇", "盈烁", "岚如", "昱雅", "菡喧", "荞蕾", "姝孺", "喧抒", "莹奕", "若翡", "雅纾", "亚娇", "迎蔓", "曼荷", "芹抒", "雅琳", "芯娅", "亦焓", "辰嘉", "浩焓", "琴羽", "昕得", "昔潇", "依薪", "颢函", "雅信", "懿语", "歆淇", "煜琼", "茹莺", "文洳", "瑞文", "茹喧", "颢阳", "淑渲", "茵宸", "蓉岩", "耘为", "颜璐", "抒孺", "莉雯", "睛菲", "彤雅", "书函", "语云", "淑芸", "雪芹", "懿宏", "从耘", "雯箫", "忆融", "予姝", "致萱", "美耘", "涵清", "欢月", "珺媛", "丝雯", "岚瑛", "岚涵", "雯碧", "淑菲", "媛岚", "岚晴", "惠菲", "涵絮", "寒舞", "絮华", "茹裳", "茹雅", "惠雯", "晴语", "涵雁", "翔雅", "云岚", "茜婷", "舒菡", "晴嘉", "絮嫣", "茵茜", "淑嫣", "雯茜", "涵媚", "雅梦", "媛珺", "岚语", "云惠", "岚嫣", "涵雅", "翔梦", "茵华", "云茜", "清岚", "惠媚", "雁菱", "茜语", "晴絮", "雅绿", "云雅", "涵雯", "舒婷", "珺裳", "茵嘉", "茜嘉", "茜雯", "茜瑛", "涵淑", "寒梦", "惠茹", "舒雅", "雯茵", "涵瑛", "晴瑛", "寒茹", "舒语", "清晴", "惜茵", "淑婷", "媛婷", "雯鸣", "舒淑", "涵云", "雯涵", "寒菱", "淑语", "媚雅", "茜寒", "珺淑", "惠珺", "茹菡", "清嫣", "媚嘉", "惠嫣", "晴云", "雯珺", "茹雯", "涵华", "寒惠", "淑茜", "茹珺", "云舒", "珺睿", "惠雅", "珺菡", "惠睿", "晴茜", "岚嫦", "云涵", "晴惠", "涵惠", "惠絮", "涵菡", "雯婷", "寒淑", "晴清", "淑涵", "珺涵", "云华", "舒媛", "岚雅", "清华", "寒菊", "涵茵", "岚菡", "岚菲", "寒云", "茹絮", "寒媛", "岚瑜", "淑淑", "惠语", "寒华", "涵婷", "晴珺", "寒瑜", "云嫦", "茵清", "茵嫣", "惠云", "翔雯", "淑梦", "晴菡", "珺云", "清雅", "雯嘉", "雯舒", "茜菡", "云嫣", "清梦", "惠茜", "茜华", "茜茜", "舒菲", "婷雯", "翔嘉", "晴岚", "晴翠", "雅舒", "茹语", "翔媛", "惠嘉", "云絮", "茹云", "雯翔", "雅淑", "雯嫣", "岚茹", "雯淑", "茜云", "晴睿", "茜梦", "云菡", "岚萍", "茵雅", "涵菲", "茵茹", "晴茵", "岚婷", "涵语", "寒雁", "淑雅", "岚舒", "寒嫣", "涵绮", "茜珺", "淑菡", "茜舒", "媚晴", "珺媚", "岚珺", "惠嫦", "舒华", "晴媛", "涵涵", "茵睿", "媛嫣", "雯菲", "茹惠", "晴雅", "岚淑", "寒媚", "雯惠", "岚雯", "茵语", "云珺", "惠瑛", "惠舒", "茜晴", "惠菡", "云瑜", "珺菲", "寒云", "舒岚", "翔淑", "珺惠", "琦翾", "晴华", "雯菱", "茜嫣", "岚云", "媛雅", "茹嫣", "茜惠", "云梦", "惠华", "晴晴", "茜茵", "茹瑜", "茜瑜", "惠婷", "茹菱", "茜茹", "媛惠", "雯绮", "雯云", "涵瑜", "惠菱", "涵珺", "惠舞", "惠雁", "雯媛", "岚碧", "茹嘉", "絮雅", "雯絮", "寒翔", "淑岚", "舒涵", "惠媛", "茜雅", "翔瑛", "清语", "雯萍", "寻雁", "清嘉", "茜媛", "涵茹", "惠晴", "茵惠", "媛菲", "珺绮", "茹菲", "涵嫣", "翔鸣", "寒睿", "雯瑛", "惠淑", "岚雁", "茜菲", "寒岚", "茜嫦", "茜菊", "涵媛", "淑云", "舒嫣", "清云", "茵萍", "淑华", "媛语", "云晴", "寒鸣", "云清", "岚华", "淑惠", "雯语", "晴茹", "雯晴", "雯睿", "清茜", "芬迎", "恬梨", "芙婕", "宸旋", "翾琳", "桐卿", "宸英", "素娅", "花曼", "芳梨", "芳海", "素海", "宸婕", "芳敏", "宸曼", "花敏", "素若", "素英", "珊婧", "桐彩", "桑婉", "珊娅", "宸悦", "珊英", "倚婕", "芬敏", "芬雪", "珊旋", "芙卿", "玲紫", "芬旋", "珊悦", "娜娅", "夏娅", "桑曼", "素婉", "花婉", "凌旋", "桑婕", "芸海", "芝婧", "蓝莺", "素敏", "芬婉", "桐旋", "花婕", "珊若", "芙悦", "芳婉", "夏卿", "珊雪", "倩婕", "花娅", "纹彩", "芙婉", "芹曼", "芬曼", "桐娅", "素曼", "宸雪", "桐梅", "芝雪", "素雪", "娜曼", "倩婉", "芳英", "芳珠", "宸珠", "芳曼", "芬梨", "芳婧", "芳卿", "翾琪", "宸卿", "桐悦", "倩雪", "素迎", "花卿", "素婧", "娜卿", "芳甜", "桐敏", "芙娅", "珊卿", "珊曼", "夏婕", "芷雪", "纹珠", "素旋", "珍娅", "笑甜", "夏婉", "娜婕", "素婕", "娜若", "芳娅", "凌雪", "宸婉", "芝英", "夏曼", "绿夏", "筠佩", "舞桑", "菱宸", "菊娜", "语恬", "淑音", "歆沐", "虞明", "绮凌", "涵柳", "晴柔", "虞青", "诗英", "欢蕾", "诗沁", "尔珍", "淑柳", "清盈", "翠芙"};
    //随机路由名称前缀
    private static final String[] ROUTER_NAMES = {"TP-LINK_", "TP-LINK_", "TP-LINK_", "TP-LINK_", "TP-LINK_", "FAST_", "FAST_", "FAST_", "Tenda_", "Tenda_", "Feixun_", "HUAWEI-"};
    //随机运营商名称
    private static final String[] PROVIDER_NAMES = {"中国移动", "中国移动", "中国移动", "中国移动", "中国联通", "中国联通", "中国联通", "中国电信", "中国电信"};
    public final int type;
    public final Callback callback;
    public final AtomicLong online;
    private final AtomicReference<String> grpcApi;
    private final AtomicReference<String> grpcApp;
    private final AtomicReference<WxClient> longConn;//长链接实例
    private final AtomicReference<LocalShort> shortConn;//短链接实例
    private final AtomicReference<String> longServer;
    private final AtomicReference<String> shortServer;
    public final String id;//当前微信实例对象ID
    private final AtomicReference<String> uuid;//二维码登录的UUID
    private final AtomicReference<byte[]> seeds;//设备实例参数种子字节
    private final AtomicReference<byte[]> notifyKey;//二维码登录的密钥
    private final AtomicReference<byte[]> longHead;//长连接包头
    private final AtomicReference<String> userName;//登录微信用户名
    public final AtomicReference<String> nickName;//登录微信用户名
    public final AtomicReference<String> password;//登录微信密码
    public final AtomicReference<String> userData;//用户设备62数据
    public final AtomicReference<String> userA16;//用户设备62数据
    public final AtomicReference<String> deviceId;//设备Id
    public final AtomicReference<String> deviceMac;//设备Mac
    public final AtomicReference<String> deviceUuid;//设备Uuid
    public final AtomicReference<String> deviceType;//设备类型
    public final AtomicReference<String> deviceName;//设备名字
    public final AtomicReference<String> versions;//登录微信用户名
    public final AtomicReference<User.Builder> userBuilder;//GRPC交互用户信息
    public final AtomicReference<WechatApiMsg> wechatApiMsg;//前段请求数据
    public final AtomicReference<WechatReturn> wechatReturn;//返回给前端的数据
    public final AtomicReference<String> cdnIp;//CDN服务器IP地址
    public final AtomicReference<byte[]> cdnKey;//CDN服务器同步密钥
    public final AtomicInteger cdnExpiry;//CDN密钥有效期
    public final AtomicInteger cdnNum;//CDN对应微信编号（可能与UIN相同）
    public final AtomicInteger cdnSeq;//CDN发包序号
    public final AtomicInteger protocolVer;//登录协议类型
    private final MessageBox messageBox;//消息队列处理器


    /**
     * 微信实例
     *
     * @param type     微信协议类型
     * @param callback 回调接口
     */
    public WechatIns(int type, Callback callback, WechatApiMsg apiMsg) throws InvalidProtocolBufferException {
        this.wechatApiMsg = new AtomicReference<>(null);
        this.wechatReturn = new AtomicReference<>(null);
        this.online = new AtomicLong(0l);
        this.messageBox = new MessageBox();
        this.grpcApi = new AtomicReference<>(GRPC_NAME_API);
        this.grpcApp = new AtomicReference<>(GRPC_NAME_APP);
        this.longConn = new AtomicReference<>(null);
        this.shortConn = new AtomicReference<>(null);
        this.longServer = new AtomicReference<>(WX_SERVER_LONG);
        this.shortServer = new AtomicReference<>(WX_SERVER_SHORT);
        this.uuid = new AtomicReference<>(null);
        this.seeds = new AtomicReference<>(WechatTool.randomBytes(SEEDS_SIZE));
        this.notifyKey = new AtomicReference<>(null);
        this.longHead = new AtomicReference<>(null);
        this.userName = new AtomicReference<>(null);
        this.nickName = new AtomicReference<>(null);
        this.password = new AtomicReference<>(null);
        this.userData = new AtomicReference<>(null);
        this.deviceType = new AtomicReference<>(null);
        this.deviceUuid = new AtomicReference<>(null);
        this.deviceName = new AtomicReference<>(null);
        this.deviceId = new AtomicReference<>(null);
        this.userBuilder = new AtomicReference<>(null);
        this.userA16 = new AtomicReference<>(null);
        this.deviceMac = new AtomicReference<>(null);
        this.cdnIp = new AtomicReference<>(null);
        this.versions = new AtomicReference<>(null);
        this.cdnKey = new AtomicReference<>(null);
        this.cdnExpiry = new AtomicInteger(0);
        this.cdnNum = new AtomicInteger(0);
        this.cdnSeq = new AtomicInteger(0);
        this.protocolVer = new AtomicInteger(0);

        if (apiMsg != null) {
            this.wechatApiMsg.set(apiMsg);
        }
        if (apiMsg.randomId == null || apiMsg.randomId.isEmpty()) {
            apiMsg.randomId = WechatTool.randomUUID();
        }
        if (apiMsg.protocolVer > 0) {
            this.protocolVer.set(apiMsg.protocolVer);
            this.type = this.protocolVer.get();
        } else {
            this.type = type;
        }
        this.id = apiMsg.randomId;
        this.callback = callback;
        this.versions.set("7.0.4");

        if (apiMsg.longHost != null && !apiMsg.longHost.isEmpty()) {
            this.longServer.set(apiMsg.longHost);
        }
        if (apiMsg.shortHost != null && !apiMsg.shortHost.isEmpty()) {
            this.shortServer.set(apiMsg.shortHost);
        }
        if (apiMsg.userUuid != null && !apiMsg.userUuid.isEmpty()) {
            this.uuid.set(apiMsg.userUuid);
            this.deviceUuid.set(apiMsg.userUuid);
        }
        if (apiMsg.userName != null && !apiMsg.userName.isEmpty()) {
            this.userName.set(apiMsg.userName);
        }
        if (apiMsg.userPassWord != null && !apiMsg.userPassWord.isEmpty()) {
            this.password.set(apiMsg.userPassWord);
        }
        if (apiMsg.userData != null && !apiMsg.userData.isEmpty()) {
            this.userData.set(apiMsg.userData);
        }
        if (apiMsg.userA16 != null && !apiMsg.userA16.isEmpty()) {
            this.userA16.set(apiMsg.userA16);
            this.deviceUuid.set(apiMsg.userA16);
        }
        if (apiMsg.grpcUser != null && apiMsg.userA16 != null && !apiMsg.userA16.isEmpty()) {
            User.Builder builder = User.newBuilder();
            builder.mergeFrom(HexStringToBinary(apiMsg.grpcUser));
            this.userBuilder.set(builder);
            if (userBuilder.get().getDeviceType() != null && !userBuilder.get().getDeviceType().isEmpty()) {
                this.deviceType.set(userBuilder.get().getDeviceType());
            }
            if (userBuilder.get().getDeviceName() != null && !userBuilder.get().getDeviceName().isEmpty()) {
                this.deviceName.set(userBuilder.get().getDeviceName());
            }
            if (userBuilder.get().getDeviceId() != null && !userBuilder.get().getDeviceId().isEmpty()) {
                this.deviceId.set(userBuilder.get().getDeviceId());
            }
        }


    }

    /**
     * 判断微信号是否登录过
     *
     * @return 是否登录过
     */
    public boolean logined() {
        User.Builder builder = userBuilder.get();
        return builder != null && builder.getUin() > 0
                && !builder.getSessionKey().isEmpty()
                && !builder.getCookies().isEmpty();
    }

    /**
     * 判断微信号是否为在线状态
     *
     * @return 是否在线
     */
    public boolean online() {
        return online.get() > 0;
    }

    /**
     * 设置设备名称
     *
     * @param str 设备名称
     * @return 当前微信实例
     */
    public WechatIns deviceName(String str) {
        deviceName.set(str);
        return this;
    }

    /**
     * 设置设备名称
     *
     * @param str 设备名称
     * @return 当前微信实例
     */
    public WechatIns deviceId(String str) {
        deviceId.set(str);
        return this;
    }

    /**
     * 设置设备名称
     *
     * @param str 设备名称
     * @return 当前微信实例
     */
    public WechatIns deviceType(String str) {
        deviceType.set(str);
        return this;
    }

    /**
     * 设置设备名称
     *
     * @param str 设备名称
     * @return 当前微信实例
     */
    public WechatIns deviceMac(String str) {
        deviceMac.set(str);
        return this;
    }

    /**
     * 设置设备名称
     *
     * @param str 设备名称
     * @return 当前微信实例
     */
    public WechatIns deviceUuid(String str) {
        deviceUuid.set(str);
        return this;
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    public String deviceId() {
        return deviceId.get();
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    public String deviceUuid() {
        return deviceUuid.get();
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    public String deviceMac() {
        return deviceMac.get();
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    public String deviceType() {
        return deviceType.get();
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    public String deviceName() {
        return deviceName.get();
    }

    /**
     * 获取当前登录微信号
     *
     * @return 当前登录微信号
     */
    public String getUserName() {
        return getUsername();
    }

    public String getCdnIp() {
        return cdnIp.get();
    }

    public byte[] getCdnKey() {
        return cdnKey.get();
    }

    public int getCdnExpiry() {
        return cdnExpiry.get();

    }

    public int getCdnNum() {
        return cdnNum.get();
    }

    public int getCdnSeq() {
        return cdnSeq.incrementAndGet();
    }


    /**
     * 判断一个微信号是否为当前登录微信号
     *
     * @param username 微信号
     * @return 是否为当前登录微信号
     */
    public boolean isSelf(String username) {
        return username != null && username.equals(getUserName());
    }

    /**
     * 判断一个微信号是否为群
     *
     * @param username 微信号
     * @return 是否为群
     */
    public boolean isGroup(String username) {
        return username != null && username.contains("@");
    }

    /**
     * 获取当前实例的User对象
     *
     * @return 当前实例的User对象
     */
    @Override
    public User getUser() {
        User.Builder builder = userBuilder.get();
        if (builder != null) {
            return builder.build();
        }
        return null;
    }

    /**
     * 是否包含CheckClient数据
     *
     * @return 是或者否
     */
    @Override
    public boolean hasClientCheckDat() {
        return type == WechatIns.TYPE_IPAD;
    }

    /**
     * 生成指定长度的字节数组
     *
     * @param name   相关名称
     * @param length 数据字节长度
     * @return 指定长度的字节数组
     */
    public byte[] genBytes(String name, int length) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = name.getBytes(WechatConst.CHARSET);
        int count = 0, times = 0;
        while (count < length) {
            byte[] part = Integer.toString(++times).getBytes(WechatConst.CHARSET);
            byte[] digest = WechatTool.md5(seeds.get(), data, part);
            try {
                out.write(digest);
            } catch (IOException e) {
                return null;
            }
            count += digest.length;
        }
        byte[] result = out.toByteArray();
        return WechatTool.cutBytes(result, 0, length);
    }

    /**
     * 生成指定长整型数字
     *
     * @param name 相关名称
     * @return 指定长整型数字
     */
    public long genNumber(String name) {
        long value = 0l;
        for (byte b : genBytes(name, 7)) {
            int val = b >= 0 ? b : b + 256;
            value *= 256;
            value += val;
        }
        return value;
    }

    /**
     * 生成指定长度的十六进制字符串
     *
     * @param name   相关名称
     * @param length 字符串长度
     * @return 指定长度的十六进制字符串
     */
    public String genHex(String name, int length) {
        int length2 = length / 2 + 1;
        byte[] data = genBytes(name, length2);
        String hex = WechatTool.bytesToHex(data);
        return hex.substring(0, length).toUpperCase();
    }

    /**
     * 生成设备唯一UUID
     *
     * @return 设备唯一UUID
     */
    public String genUUID() {
        if (deviceUuid.get() == null || deviceUuid.get().isEmpty()) {
            byte[] data = genBytes("uuid", 16);
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++)
                msb = (msb << 8) | (data[i] & 0xff);
            for (int i = 8; i < 16; i++)
                lsb = (lsb << 8) | (data[i] & 0xff);
            deviceUuid.set(new UUID(msb, lsb).toString());
        }
        return deviceUuid.get();
    }

    /**
     * 生成设备网络MAC地址
     *
     * @return 设备网络MAC地址
     */
    public String genMac() {
        if (deviceMac.get() == null || deviceMac.get().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (char ch : genHex(genUUID(), 12).toCharArray()) {
                if (count > 0 && count % 2 == 0) {
                    sb.append(':');
                }
                sb.append(Character.toLowerCase(ch));
                count++;
            }
            deviceMac.set(sb.toString());
        }
        return deviceMac.get();
    }

    /**
     * 获取软件类型相关XML
     *
     * @return 软件类型相关XML
     */
    public String genSoftType() {
        if (deviceType.get() == null || deviceType.get().isEmpty()) {
            long index1 = genNumber("router");
            long index2 = genNumber("operator");
            String router = ROUTER_NAMES[(int) (index1 % ROUTER_NAMES.length)];
            String provider = PROVIDER_NAMES[(int) (index2 % PROVIDER_NAMES.length)];
            router += genHex("router", 6);
            deviceType.set("<k21>" + router + "</k21><k22>" + provider + "</k22><k24>" + genMac() + "</k24>");
        }
        return deviceType.get();
    }

    /**
     * 生成设备ID
     *
     * @return 设备ID
     */
    public String genDeviceId() {
        if (deviceId.get() == null || deviceId.get().isEmpty()) {
            deviceId.set(getDevideID(genUUID()));
        }
        return deviceId.get();
    }

    /**
     * 生成设备62数据
     *
     * @return 设备62数据
     */
    public String genDeviceDat() {
        if (userData.get() == null || userData.get().isEmpty()) {
            String deviceId = genDeviceId();
            String deviceHex = WechatTool.bytesToHex(deviceId.getBytes(WechatConst.CHARSET));
            userData.set("62706C6973743030D4010203040506090A582476657273696F6E58246F626A65637473592461726368697665725424746F7012000186A0A2070855246E756C6C5F1020"
                    + deviceHex
                    + "5F100F4E534B657965644172636869766572D10B0C54726F6F74800108111A232D32373A406375787D0000000000000101000000000000000D0000000000000000000000000000007F");
        }
        return userData.get();
    }

    public String genUserA16() {
        return userA16.get();
    }

    public void userA16(String a16) {
        userA16.set(a16);
    }


    /**
     * 生成设备名称
     *
     * @return 设备名称
     */
    public String genDeviceName() {
        if (deviceName.get() == null || deviceName.get().isEmpty()) {
            long index = genNumber("name");
            String person = PERSON_NAMES[(int) (index % PERSON_NAMES.length)];
            switch (type) {
                case TYPE_IPAD:
                    deviceName.set(person + "的 iPad");//iPad名称
                case TYPE_IMAC:
                    deviceName.set(person + "的 iMac");//iMac名称
                default:
                    return null;
            }
        }
        return deviceName.get();
    }

    /**
     * 生成设备类型
     *
     * @return 设备类型
     */
    @Override
    public String genDeviceType() {
        switch (type) {
            case TYPE_IPAD:
                return "iPad iPhone OS9.3.3";//iPad设备版本号
            case TYPE_IMAC:
                return "iMac iPhone OS9.3.3";//iMac设备版本号
            default:
                return null;
        }
    }

    /**
     * 生成微信版本
     *
     * @return 微信版本
     */
    public String getVersion() {
        switch (type) {
            case TYPE_IPAD:
                return VERSION_IPAD;
            case TYPE_IPHONE:
                return VERSION_IPAD;
            case TYPE_IMAC:
                return VERSION_IMAC;
            case TYPE_ANDROID:
                return VERSION_IMAC;
            case TYPE_WINDOWS:
                return VERSION_IMAC;
            case TYPE_WINPHONE:
                return VERSION_IMAC;
            case TYPE_QQC:
                return VERSION_IMAC;
        }
        return null;
    }

    /**
     * 获取微信版本数字
     *
     * @return 微信版本数字
     */
    @Override
    public int genClientVersion() {
        switch (type) {
            case TYPE_IPAD:
                return VERSION_IPAD_060620;//iPad微信版本号
            case TYPE_IMAC:
                return VERSION_IMAC_020312;//iMac微信版本号
            default:
                return 0;
        }
    }

    /**
     * 生成GRPC协议版本
     *
     * @return GRPC协议版本
     */
    public int getProtocolVer() {
        switch (type) {
            case WechatIns.TYPE_IPAD:
                return 1;
            case WechatIns.TYPE_IMAC:
                return 3;
        }
        return 1;
    }

    /**
     * 设置功能GRPC链接池名称
     *
     * @param app 基础功能
     * @param api 加好友功能
     * @return 本微信实例
     */
    public WechatIns grpc(String app, String api) {
        if (app != null) {
            grpcApp.set(app);
        }
        if (api != null) {
            grpcApi.set(api);
        }
        return this;
    }

    /**
     * 获取微信本地短链接实例
     *
     * @return 本地短链接实例
     */
    public LocalShort shortConn() {
        return shortConn(false);
    }

    /**
     * 获取微信本地短链接实例
     *
     * @param reconnect 是否重连
     * @return 本地短链接实例
     */
    public LocalShort shortConn(boolean reconnect) {
        synchronized (shortConn) {
            LocalShort conn = shortConn.get();
            if (conn != null && !conn.working()) {
                conn.dispose();//释放失效链接并重连
                conn = null;
            } else if (conn != null && reconnect) {
                conn.dispose();//强制重新连接服务器
                conn = null;
            }
            if (conn == null) {
                String server = shortServer.get();
                conn = new LocalShort(this, server, 80);
                conn.init();
                shortConn.set(conn);
            }
            return conn;
        }
    }

    /**
     * 初始化微信实例（指定种子）
     *
     * @param account 种子字符串
     * @return 实例本身
     */
    public WechatIns init(String... account) {
        this.seeds.set(WechatTool.md5(account));
        return init();
    }

    /**
     * 初始化微信实例（指定种子）
     *
     * @param seeds 种子数据
     * @return 实例本身
     */
    public WechatIns init(byte[] seeds) {
        this.seeds.set(WechatTool.md5(seeds));
        return init();
    }

    /**
     * 初始化微信实例（随机种子）
     *
     * @return 实例本身
     */
    public WechatIns init() {
        return this;
    }

    /**
     * 释放微信实例，断开相关微信连接
     */
    public void dispose() {
        messageBox.dispose();
        synchronized (longConn) {
            WxClient conn = longConn.getAndSet(null);
            if (conn != null) {
                conn.shutdown();
            }
        }
        synchronized (shortConn) {
            LocalShort conn = shortConn.getAndSet(null);
            if (conn != null) {
                conn.dispose();
            }
        }
    }


    /**
     * 获取自动登录封包
     *
     * @return 自动登录数据包
     */
    public WechatObj.AutoPack getAutoPack() {
        WechatObj.AutoPack pack = new WechatObj.AutoPack();
        pack.Type = type;
        pack.LongServer = longServer.get();
        wechatApiMsg.get().longHost(pack.LongServer);
        pack.ShortServer = shortServer.get();
        wechatApiMsg.get().shortHost(pack.ShortServer);
        pack.UserData = null;
        User.Builder builder = userBuilder.get();
        if (builder != null) {
            byte[] userData = builder.build().toByteArray();
            pack.UserData = Base64.getEncoder().encodeToString(userData);
            wechatApiMsg.get().grpcUser(pack.UserData);
        }

        return pack;
    }

    /**
     * 设置自动登录数据包
     *
     * @param pack 自动登录数据包
     * @return 是否设置成功
     */
    public boolean setAutoPack(WechatObj.AutoPack pack) {
        if (pack != null && pack.Type == type) {
            try {
                longServer.set(pack.LongServer);
                shortServer.set(pack.ShortServer);
                byte[] userData = Base64.getDecoder().decode(pack.UserData);
                User user = User.parseFrom(userData);
                User.Builder builder = User.newBuilder(user);
                userBuilder.set(builder);
                longConn(true);
                return true;
            } catch (Exception e) {
                WechatDebug.echo(e);
            }
        }
        return false;
    }

    /**
     * 同步新消息
     */
    public void newSync() {
        BaseMsg baseMsgBuilder;
        if (false) {
            BaseMsg.Builder builder = BaseMsg.newBuilder()
                    .setCmd(ApiCmd.CMD_NEW_SYNC)
                    .setCmdUrl("/cgi-bin/micromsg-bin/newsync")
                    .setUser(userBuilder.get());
            byte[] bytes = packNewSyncRequest(builder.build());
            BaseMsg.Builder builder1 = longCall(builder.setPayloads(ByteString.copyFrom(bytes)));
            baseMsgBuilder = unpackNewSyncResponse(builder1.build());
        } else {
            WechatMsg msg = longCall(ApiCmd.CMD_NEW_SYNC, null);
            baseMsgBuilder = msg.getBaseMsg();
        }
        List<WechatObj.Message> list = parseList(baseMsgBuilder, WechatObj.Message[].class);
        if (list != null) {
            WechatDebug.log(this, "WechatIns.newSync", "list(" + list.size() + ")");
            for (WechatObj.Message message : list) {
                messageBox.push(this, message);
            }
        }
    }

    public void solveCmdItem(WechatIns ins, WechatProto.CmdItem item, boolean async) {
        try {
            if (item.cmdId == 2) {
                //好友列表
                WechatProto.ModContact contact = WechatProto.ModContact.parse(item.cmdBuf.buffer);

                if (contact != null) {
                    WechatDebug.log(ins, "WechatIns.Callback.solveCmdItem",
                            "ModContact=" + (contact.nickName != null ? contact.nickName.str : "NULL"));
                    if (async) {

                        modContactToContactInfo(contact);
                    }

                }
            } else if (item.cmdId == 5) {
                //未读消息
                WechatProto.AddMsg msg = WechatProto.AddMsg.parse(item.cmdBuf.buffer);

                if (msg != null) {
                    WechatDebug.log(ins, "WechatIns.Callback.solveCmdItem",
                            "AddMsg={" + msg.msgType + "}" + msg.createTime);
                    if (async) {
                        ins.messageBox.push(ins, addMsgToMessage(msg));
                    }
                }
            }
        } catch (Exception e) {
            WechatDebug.echo(e);
        }
    }

    public WechatObj.Message addMsgToMessage(WechatProto.AddMsg addMsg) {
        WechatObj.Message message = new WechatObj.Message();
        message.setMsgId(addMsg.msgId);
        message.setFromUserName(addMsg.fromUserName.str);//发送者
        message.setToUserName(addMsg.toUserName.str);//接受者
        message.setMsgType(addMsg.msgType);//消息类型
        message.setContent(addMsg.content.str);//内容
        message.setStatus(addMsg.status);//状态
        message.setImgStatus(addMsg.imgStatus);//1推送图片,2普通图片
        message.setImgBuf(addMsg.imgBuf);//图片
        message.setCreateTime(addMsg.createTime);//消息发送时间戳
        message.setMsgSource(addMsg.msgSource);//消息来源
        message.setPushContent(addMsg.pushContent);//推送
        message.setNewMsgId(addMsg.newMsgId);//新消息ID
        return message;
    }

    /**
     * 获取的联系人信息
     */
    public WechatObj.ContactInfo modContactToContactInfo(WechatProto.ModContact modContact) {
        WechatObj.ContactInfo contactInfo = new WechatObj.ContactInfo();
        contactInfo.setMsgType(modContact.source);//消息ID
        contactInfo.setUserName(modContact.userName.str);//微信号，陌生人时为v1数据
        contactInfo.setNickName(modContact.nickName.str);//昵称
        contactInfo.setSignature(modContact.signature);//签名
        contactInfo.setSmallHeadImgUrl(modContact.smallHeadImgUrl);//小头像
        contactInfo.setBigHeadImgUrl(modContact.bigHeadImgUrl);//大头像
        contactInfo.setProvince(modContact.province);//省份
        contactInfo.setCity(modContact.city);//城市
        contactInfo.setRemark(modContact.remark.str);//备注
        contactInfo.setAlias(modContact.alias);//签名
        contactInfo.setSex(modContact.sex);//性别
        contactInfo.setContactType(modContact.contactType);//联系人类型
        contactInfo.setVerifyFlag(modContact.verifyFlag);//验证标志
        contactInfo.setLabelLists(modContact.labelIDList);
        contactInfo.setChatRoomOwner(modContact.chatRoomOwner);
        contactInfo.setEncryptUsername(modContact.encryptUserName);
        contactInfo.setExtInfo(modContact.extInfo);//陌生人时为微信号
        contactInfo.setExtInfoExt(modContact.chatRoomOwner);//签名
        contactInfo.setTicket(modContact.chatRoomOwner);//陌生人时为v2数据
        contactInfo.setChatroomVersion(modContact.chatroomVersion);//验证标志
        return contactInfo;
    }

    /**
     * 获取登录二维码
     *
     * @return 登录二维码对象
     */
    public WechatObj.LoginQrcode getLoginQrcode() {
        WechatMsg msg = shortCall(ApiCmd.CMD_GET_LOGIN_QRCODE, null);
        WechatObj.LoginQrcode result = parseObject(msg, WechatObj.LoginQrcode.class);
        if (result != null) {
            byte[] notifyKeyData = Base64.getDecoder().decode(result.NotifyKey);
            uuid.set(result.Uuid);
            notifyKey.set(notifyKeyData);
        }
        return result;
    }

    /**
     * 检查登录二维码
     *
     * @return 登录二维码对象
     */
    public WechatObj.LoginQrcode checkLoginQrcode() {
        WechatMsg msg = shortCall(ApiCmd.CMD_CHECK_LOGIN_QRCODE, null);
        WechatObj.LoginQrcode result = parseObject(msg, WechatObj.LoginQrcode.class);
        if (result != null) {
            if (result.Status == 2) {
                userName.set(result.Username);
                password.set(result.Password);
            }
        }
        return result;
    }

    /**
     * 发送二维码登录请求
     *
     * @return 是否登录成功
     */
    public boolean doLoginQrcode() {
        return doLogin(ApiCmd.CMD_DO_LOGIN_QRCODE);
    }

    /**
     * 发送62登录请求
     *
     * @return 是否登录成功
     */
    public boolean doUserLogin(String userDat) {
        userData.set(userDat);
        return doLogin(ApiCmd.CMD_DO_USER_LOGIN);
    }

    /**
     * 发送a16登录请求
     *
     * @return 是否登录成功
     */
    public boolean doA16Login(String A16) {
        userA16.set(A16);
        return doLogin(ApiCmd.CMD_DO_ANDROID_LOGIN);
    }

    /**
     * 发送二维码登录请求
     *
     * @return 是否登录成功
     */
    public boolean doQQLogin() {
        return doLogin(ApiCmd.CMD_DO_QQ_LOGIN);
    }

    /**
     * 发送自动登录请求
     *
     * @return 是否登录成功
     */
    public boolean doLoginAuto() {
        HashMap<String, Object> params = new HashMap<>();
        return doLogin(ApiCmd.CMD_DO_LOGIN_AUTO, params);
    }

    public boolean doLogin(int cmd) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("Username", userName.get());
        param.put("PassWord", password.get());
        param.put("UUid", genUUID());
        param.put("ProtocolVer", protocolVer.get());
        param.put("DeviceType", genSoftType());
        param.put("DeviceName", genDeviceName());
        param.put("Language", "zh_CN");
        param.put("RealCountry", "CN");
        if (cmd == 3333) {
            param.put("UUid", genUserA16());
            param.put("versions", versions.get());
            param.put("DeviceMac", genMac());
            param.put("OSType", "android-25");
            param.put("DeviceBrand", "Xiaomi");
            param.put("DeviceType", "android-23");
            param.put("DeviceName", "HM NOTE 1TD");
            param.put("DeviceModel", "armeabi-v7a");
            param.put("DeviceID", genUserA16());
            param.put("DeviceIMEI", WechatUtil.getImei());
            param.put("DeviceAndroid", WechatUtil.getImsi());
        }
        return doLogin(cmd, param);
    }

    //推送消息到 webSocket
    public void sendMsg() {
        sendMsg(wechatReturn.get());
    }

    public void sendMsg(Object data) {
        String datas = GsonUtil.GsonString(data);
        if (data == null || data.equals("")) {
            datas = GsonUtil.GsonString(wechatApiMsg.get());
        }
        sendMsg(wechatApiMsg.get().account, datas);
    }

    @Override
    public void sendMsg(String account, String data) {
        try {
            sendMessage(account, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 登录微信
     *
     * @param cmd    登录GRPC接口ID
     * @param params 登录参数
     * @return 是否登录成功
     */
    private boolean doLogin(int cmd, HashMap<String, Object> params) {
        WechatMsg msg = longCall(cmd, params);
        if (msg.getBaseMsg().getRet() == 0) {
            if (doHeartbeat()) {
                long now = System.currentTimeMillis();
                if (online.compareAndSet(0l, now)) {
                    callback.online(this);//上线通知
                }
                WechatDebug.log(this, "WechatIns.doLogin", "nickname=" + getNickname());
                sendMsg();
                return true;
            }
        } else if (msg.getBaseMsg().getRet() == -301) {
            longServer.set(msg.getBaseMsg().getLongHost());
            shortServer.set(msg.getBaseMsg().getShortHost());
            longConn(true);
            sendMsg();
            return doLogin(cmd, params);
        }
        sendMsg();
        return false;
    }

    public void setWxdat() {
        wechatApiMsg.get().autoLogin(true)
                .userName(userName.get())
                .nickName(nickName.get())
                .userA16(userA16.get())
                .userData(userData.get())
                .longHost(longServer.get())
                .protocolVer(protocolVer.get())
                .shortHost(shortServer.get())
                .grpcUser(BinaryToHexString(userBuilder.get().build().toByteArray()));
    }

    public WechatReturn getWxdat() {
        return setWechatReturn(wechatApiMsg.get(), 1, "当前实例状态", wechatApiMsg.get(), 9999);
    }

    /**
     * 发送心跳
     *
     * @return 是否发送成功
     */
    public boolean doHeartbeat() {
        WechatDebug.log(this, "WechatIns.doHeartbeat", "NOOP");
        WechatMsg msg = longCall(ApiCmd.CMD_HEARTBEAT, null);
        sendMsg();
        return msg != null && msg.getBaseMsg().getRet() == 0;
    }

    /**
     * 发送文本消息
     *
     * @param username 接收微信号
     * @param content  发送内容
     * @return 是否发送成功
     */
    public boolean sendMicroMsg(String username, String content) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ToUserName", username);
        params.put("Type", 0);
        params.put("Content", content);
        WechatMsg msg = longCall(ApiCmd.CMD_SEND_MIRCO_MSG, params);
        sendMsg();
        return msg != null && msg.getBaseMsg().getRet() == 0;
    }

    /**
     * 发送图片消息
     *
     * @param username 接收微信号
     * @param data     图片数据
     * @return 是否发送成功
     */
    public boolean sendImageMsg(String username, byte[] data) {
        int start = 0, total = data.length, size = 65535;
        int timeStamp = (int) (System.currentTimeMillis() / 1000);
        String clientImgId = getUserName() + "_" + timeStamp;
        HashMap<String, Object> params = new HashMap<>();
        params.put("ClientImgId", clientImgId);
        params.put("ToUserName", username);
        params.put("TotalLen", total);
        while (start < total) {
            int length = total - start > size ? size : total - start;
            byte[] pack = new byte[length];
            System.arraycopy(data, start, pack, 0, length);
            params.put("StartPos", start);
            params.put("DataLen", length);
            params.put("Data", WechatTool.bytesToInts(pack));
            start += length;
            longCall(ApiCmd.CMD_SEND_IMAGE_MSG, params, null);
        }
        return true;
    }

    /**
     * 发送图片消息
     *
     * @param username    接收微信号
     * @param fileId      CDN的URL
     * @param imgSize     图片大小
     * @param aesKey      AES密钥
     * @param fileMd5     文件MD5
     * @param thumbLength 缩略图大小
     * @param thumbHeight 缩略图高度
     * @param thumbWidth  缩略图宽度
     * @return 是否发送成功
     */
    public boolean sendImageMsg(String username,
                                String fileId, int imgSize, String aesKey, String fileMd5,
                                int thumbLength, int thumbHeight, int thumbWidth) {
        int timeStamp = (int) (System.currentTimeMillis() / 1000);
        String clientImgId = getUserName() + "_" + timeStamp;
        HashMap<String, Object> params = new HashMap<>();
        params.put("ClientImgId", clientImgId);
        params.put("ToUserName", username);
        params.put("TotalLen", imgSize);
        params.put("StartPos", 0);
        params.put("DataLen", imgSize);
        params.put("Data", WechatTool.bytesToInts(new byte[0]));
        params.put("MsgType", 3);
        params.put("NetType", 1);
        params.put("CDNBigImgUrl", "");
        params.put("CDNMidImgUrl", fileId);
        params.put("AESKey", aesKey);
        params.put("EncryVer", 1);
        params.put("CDNBigImgSize", 0);
        params.put("CDNMidImgSize", imgSize);
        params.put("CDNThumbImgUrl", fileId);
        params.put("CDNThumbImgSize", thumbLength);
        params.put("CDNThumbImgHeight", thumbHeight);
        params.put("CDNThumbImgWidth", thumbWidth);
        params.put("CDNThumbAESKey", aesKey);
        params.put("Md5", fileMd5);
        params.put("CRC32", 0);
        params.put("HitMd5", 0);
        longCall(ApiCmd.CMD_SEND_IMAGE_MSG, params, null);
        return true;
    }

    /**
     * 发送视频消息
     *
     * @param username   接收微信号
     * @param fileId     CDN的URL
     * @param videoSize  视频大小
     * @param playLength 播放时长
     * @param thumbSize  缩略图大小
     * @param aesKey     AES密钥
     * @param fileMd5    文件MD5
     * @param fileNewMd5 文件新MD5
     * @return 是否发送成功
     */
    public boolean sendVideoMsg(String username,
                                String fileId, int videoSize, int playLength, int thumbSize,
                                String aesKey, String fileMd5, String fileNewMd5) {
        int timeStamp = (int) (System.currentTimeMillis() / 1000);
        String clientMsgId = getUserName() + "_" + timeStamp;
        HashMap<String, Object> params = new HashMap<>();
        params.put("ClientMsgId", clientMsgId);
        params.put("ToUserName", username);
        params.put("ThumbTotalLen", thumbSize);
        params.put("ThumbStartPos", thumbSize);
        params.put("VideoTotalLen", videoSize);
        params.put("VideoStartPos", videoSize);
        params.put("PlayLength", playLength);
        params.put("AESKey", aesKey);
        params.put("CDNVideoUrl", fileId);
        params.put("VideoMd5", fileMd5);
        params.put("VideoNewMd5", fileNewMd5);
        longCall(ApiCmd.CMD_SEND_VIDEO_MSG, params, null);
        return true;
    }

    /**
     * 查找指定微信号数据
     *
     * @param username 微信号
     * @return 微信号查找结果
     */
    public WechatObj.ContactInfo searchContact(String username) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", username);
        WechatMsg msg = shortCall(ApiCmd.CMD_SEARCH_CONTACT, params);
        return parseObject(msg, WechatObj.ContactInfo.class);
    }

    public String getA8Key(String reqUrl, int scene, String username) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ReqUrl", reqUrl);
        params.put("Scene", scene);
        params.put("Username", username);
        WechatMsg msg = shortCall(ApiCmd.CGI_GET_A8KEY, params);
        return msg.getBaseMsg().getPayloads().toStringUtf8();
    }

    /**
     * 通过好友请求
     *
     * @param v1 陌生人v1信息
     * @param v2 陌生人v2信息
     * @return 是否通过成功
     */
    public boolean addContact(String v1, String v2) {
        return addContact(v1, v2, 3, 6, "");
    }

    /**
     * 添加好友请求
     *
     * @param v1      陌生人v1信息
     * @param v2      陌生人v2信息
     * @param opcode  通过操作码，opcode=2（主动添加好友）
     * @param scene   添加好友场景，scene=3（通过微信号搜索）
     * @param content 验证语
     * @return 是否添加成功
     */
    public boolean addContact(String v1, String v2, int opcode, int scene, String content) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("Encryptusername", v1);
        params.put("Ticket", v2);
        params.put("Type", opcode);
        params.put("Sence", scene);
        params.put("Content", content);
        WechatMsg msg = shortCall(ApiCmd.CMD_ADD_CONTACT, params);
        return msg != null && msg.getBaseMsg().getRet() == 0;
    }

    /**
     * 获取指定微信号或群的二维码
     *
     * @param username 微信号或群号
     * @return 二维码数据消息
     */
    public WechatObj.QrcodeInfo getQrcode(String username) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("Username", username);
        WechatMsg msg = shortCall(ApiCmd.CMD_GET_QRCODE, params);
        return parseObject(msg, WechatObj.QrcodeInfo.class);
    }
    /**
     * 解析推送来的消息
     *
     * @return 推送来的消息明文
     */

    /**
     * 获取指定群的成员信息
     *
     * @param chatroom 群微信号
     * @return 群相关成员信息
     */
    public WechatObj.ChatroomInfo getChatroomInfo(String chatroom) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("Chatroom", chatroom);
        WechatMsg msg = shortCall(ApiCmd.CMD_GET_CHATROOM_MEMBER, params);
        return parseObject(msg, WechatObj.ChatroomInfo.class);
    }

    /**
     * 删除群成员
     *
     * @param chatroom 微信群号
     * @param member   群成员微信号
     * @return 是否删除成功
     */
    public boolean delChatroomMember(String chatroom, String member) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ChatRoom", chatroom);
        params.put("Username", member);
        WechatMsg msg = shortCall(ApiCmd.CMD_DEL_CHATROOM_MEMBER, params);
        return msg != null && msg.getBaseMsg().getRet() == 0;
    }


    /**
     * 发布群公告
     *
     * @param chatroom     微信群号
     * @param announcement 群公告内容
     * @return 是否发布成功
     */
    public boolean setChatRoomAnnouncement(String chatroom, String announcement) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ChatRoomName", chatroom);
        params.put("Announcement", announcement);
        WechatMsg msg = shortCall(ApiCmd.CMD_SET_CHATROOM_ANNOUNCEMENT, params);
        return msg != null && msg.getBaseMsg().getRet() == 0;
    }

    /**
     * 发送朋友圈（原始XML）
     *
     * @param content 朋友圈内容XML
     * @return 发送结果XML
     */
    public String snsPost(String content) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("Content", WechatTool.bytesToInts(content.getBytes(WechatConst.CHARSET)));
        WechatMsg msg = shortCall(ApiCmd.CMD_SNS_POST, params);
        if (msg != null && msg.getBaseMsg().getRet() == 0) {
            return msg.getBaseMsg().getPayloads().toStringUtf8();
        }
        return null;
    }

    /**
     * 发送朋友圈点赞或评论
     *
     * @param id       朋友圈ID
     * @param username 发送给用户的微信号
     * @param type     类型：1=点赞，2=评论
     * @param content  朋友圈内容
     * @return 发送结果
     */
    public String snsComment(String id, String username, int type, String content) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ID", id);
        params.put("ToUsername", username);
        params.put("Type", type);
        if (content != null) {
            params.put("Content", content);
        }
        WechatMsg msg = shortCall(ApiCmd.CMD_SNS_COMMENT, params);
        if (msg != null && msg.getBaseMsg().getRet() == 0) {
            return msg.getBaseMsg().getPayloads().toStringUtf8();
        }
        return null;
    }

    /**
     * 上传朋友圈图片
     *
     * @param data 图片数据
     * @return 上传结果
     */
    public String snsUploadImage(byte[] data) {
        int start = 0, total = data.length, size = 65535;
        String clientImgId = getUserName() + "_" + System.currentTimeMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("ClientId", clientImgId);
        params.put("TotalLen", total);
        WechatMsg msg = null;
        while (start < total) {
            int length = total - start > size ? size : total - start;
            byte[] pack = new byte[length];
            System.arraycopy(data, start, pack, 0, length);
            params.put("StartPos", start);
            params.put("Uploadbuf", WechatTool.bytesToInts(pack));
            start += length;
            if (start < total) {
                longCall(ApiCmd.CMD_SNS_UPLOAD, params, null);
            } else {
                msg = longCall(ApiCmd.CMD_SNS_UPLOAD, params);
            }
        }
        if (msg != null && msg.getBaseMsg().getRet() == 0) {
            return msg.getBaseMsg().getPayloads().toStringUtf8();
        }
        return null;
    }

    /**
     * 登出微信
     *
     * @return 是否登出成功
     */
    public boolean doLogout() {
        WechatMsg msg = shortCall(ApiCmd.CMD_LOGOUT, null);
        return msg != null && msg.getBaseMsg().getRet() == 0;
    }

    /**
     * 将消息对象转换为结果对象
     *
     * @param msg 返回的消息，BaseMsg中Payloads为JSON字符串
     * @param cls 转换的结果对象类
     * @param <T> 类参数
     * @return 转换后的结果对象
     */
    private <T> T parseObject(WechatMsg msg, Class<T> cls) {
        if (msg != null && msg.getBaseMsg().getRet() == 0) {
            String json = msg.getBaseMsg().getPayloads().toStringUtf8();
            if (json == null || json.isEmpty() || "null".equalsIgnoreCase(json)) {
                return null;
            }
            return WechatTool.gsonObj(json, cls);
        }
        return null;
    }

    /**
     * 将消息对象转换为结果列表
     *
     * @param msg 返回的消息，BaseMsg中Payloads为JSON字符串
     * @param cls 转换的结果对象类
     * @param <T> 类参数
     * @return 转换后的结果列表
     */
    private <T> List<T> parseList(BaseMsg msg, Class<T[]> cls) {
        if (msg != null && msg.getRet() == 0) {
            String json = msg.getPayloads().toStringUtf8();
            if (json == null || json.isEmpty() || "null".equalsIgnoreCase(json)) {
                return new ArrayList<>();
            }
            return WechatTool.gsonList(json, cls);
        }
        return null;
    }

    /**
     * 将消息对象转换为结果列表
     *
     * @param msg 返回的消息，BaseMsg中Payloads为JSON字符串
     * @param cls 转换的结果对象类
     * @param <T> 类参数
     * @return 转换后的结果列表
     */
    private <T> List<T> parseList(WechatMsg msg, Class<T[]> cls) {
        return parseList(msg.getBaseMsg(), cls);
    }

    /**
     * 封包请求
     *
     * @param code   接口ID
     * @param params 接口参数
     * @return 封包结果
     */
    public WechatMsg grpcPack(int code, HashMap<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("ProtocolVer", getProtocolVer());
        int cmd = code;
        byte[] data = WechatTool.gsonString(params).getBytes(WechatConst.CHARSET);
        WechatMsg msg = grpcCall(cmd, data);
        if (msg != null) {
            if (code == ApiCmd.CMD_GET_LOGIN_QRCODE) {
                longHead.set(msg.getBaseMsg().getLongHead().toByteArray());
            }
        }
        return msg;
    }

    /**
     * 解包请求
     *
     * @param code 接口ID
     * @param data 接口数据
     * @return 解包结果
     */
    public WechatMsg grpcUnpack(int code, byte[] data) {
        if (WechatConst.LOCAL) {


        }
        int cmd = code;
        if (code > 0) {
            cmd = -code;//切换接口ID为解包ID
        }
        if (cmd == -1111 || cmd == -2222 || cmd == -3333) {
            cmd = -1001;
        } else if (cmd == -211) {
            cmd = -212;
        }
        WechatMsg msg = grpcCall(cmd, data);
        if (msg != null) {
            BaseMsg baseMsg = msg.getBaseMsg();
            if (baseMsg.getRet() == 0) {
                User.Builder builder = userBuilder.get();
                if (builder != null) {
                    builder.mergeFrom(baseMsg.getUser());
                }
            } else if (baseMsg.getRet() == 301) {
                longServer.set(baseMsg.getLongHost());
                shortServer.set(baseMsg.getShortHost());
            }
        }
        return msg;
    }

    /**
     * GRPC请求
     *
     * @param cmd  接口ID
     * @param data 接口数据
     * @return 返回数据对象
     */
    public WechatMsg grpcCall(int cmd, byte[] data) {
        String grpcName = grpcApp.get();
        GrpcClient grpcClient = GrpcClient.get(grpcName);
        if (grpcClient != null) {
            WechatMsg.Builder builder = WechatMsg.newBuilder()
                    .setBaseMsg(packBaseMsg(cmd, data))
                    .setToken(grpcClient.token)
                    .setVersion(getVersion())
                    .setTimeStamp((int) (System.currentTimeMillis() / 1000l))
                    .setIP("127.0.0.1");
            WechatMsg msg = builder.build();
            WechatMsg result = grpcClient.call(msg, GRPC_RETRY);
            if (WechatConst.PAYLOADS && cmd > 0) {
                byte[] packed = result.getBaseMsg().getPayloads().toByteArray();
                byte[] session = result.getBaseMsg().getUser().getSessionKey().toByteArray();
                System.out.println("===== PACK:" + cmd + "-" + WechatTool.bytesToHex(session) + " =====");
                System.out.println(WechatTool.bytesToHex(packed));
            }
            return result;
        }
        return null;
    }

    /**
     * 长连接请求
     *
     * @param request
     * @return
     */
    public BaseMsg.Builder longCall(BaseMsg.Builder request) {
        byte[] data = longRequest(request.build());
        return request.setPayloads(data != null && data.length > 0 ? ByteString.copyFrom(data) : ByteString.EMPTY);
    }

    /**
     * 长连接请求
     *
     * @param code   接口ID
     * @param params 接口参数
     * @return 返回结果
     */
    public WechatMsg longCall(int code, HashMap<String, Object> params) {
        WechatMsg request = grpcPack(code, params);
        byte[] data = longRequest(request.getBaseMsg());
        return grpcUnpack(code, data);
    }

    /**
     * 长连接请求（自己控制回调）
     *
     * @param code     接口ID
     * @param params   接口参数
     * @param callback 回调接口，传null则为不关注结果的异步调用
     */
    public void longCall(int code, HashMap<String, Object> params, WxClient.Callback callback) {
        WechatMsg request = grpcPack(code, params);
        longRequest(request.getBaseMsg(), callback);
    }

    /**
     * 短连接请求
     *
     * @param code   接口ID
     * @param params 接口参数
     * @return 返回结果
     */
    public WechatMsg shortCall(int code, HashMap<String, Object> params) {
        WechatMsg request = grpcPack(code, params);
        if (request != null) {
            byte[] data = shortRequest(request.getBaseMsg());
            return grpcUnpack(code, data);
        } else return null;
    }

    /**
     * 获取微信长连接（自动连接，不重连）
     *
     * @return 微信长连接
     */
    public WxClient longConn() {
        return longConn(false);
    }

    /**
     * 获取微信长连接（自动连接，可指定重连）
     *
     * @param reconnect 是否重连
     * @return 微信长连接
     */
    public WxClient longConn(boolean reconnect) {
        synchronized (longConn) {
            WxClient conn = longConn.get();
            if (conn != null && !conn.working()) {
                conn.shutdown();//释放失效链接并重连
                conn = null;
            } else if (conn != null && reconnect) {
                conn.shutdown();//强制重新连接服务器
                conn = null;
            }
            if (conn == null) {
                conn = new WxClient(this, longServer.get(), 80);
                conn.startup();
                longConn.set(conn);
            }
            return conn;
        }
    }

    /**
     * 向微信发送长连接请求（同步）
     *
     * @param msg 请求消息
     * @return 返回数据
     */
    private byte[] longRequest(BaseMsg msg) {
        final AtomicReference<byte[]> result = new AtomicReference<>();
        final WxClient.Callback callback = new WxClient.Callback() {
            @Override
            public void onData(byte[] data) {
                result.set(data);
            }
        };
        try {
            longRequest(msg, callback);
            synchronized (callback) {
                callback.wait(TIMEOUT_LONG);
            }
        } catch (Exception e) {
            WechatDebug.echo(e);
        }
        return result.get();
    }

    /**
     * 向微信发送长连接请求（异步）
     *
     * @param msg      请求消息
     * @param callback 回调函数
     */
    private void longRequest(BaseMsg msg, WxClient.Callback callback) {
        byte[] head = msg.getLongHead().toByteArray();
        byte[] body = msg.getPayloads().toByteArray();
        byte[] data = WechatTool.joinBytes(head, body);
        byte[] rand = WechatTool.randomBytes(4);
        System.arraycopy(rand, 0, data, 12, 4);
        try {
            longConn().sendData(data, callback);
        } catch (IOException e) {
            WechatDebug.echo(e);
        }
    }

    /**
     * 向微信发送短链接请求
     *
     * @param msg 请求消息
     * @return 返回数据
     */
    private byte[] shortRequest(BaseMsg msg) {
        String url = "http://" + shortServer.get() + msg.getCmdUrl();
        byte[] data = msg.getPayloads().toByteArray();
        try {
            byte[] response = null;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            try {
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT_SHORT);
                conn.setReadTimeout(TIMEOUT_SHORT);
                conn.setRequestProperty("Charset", WechatConst.CHARSET_NAME);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Length", Integer.toString(data.length));
                OutputStream os = conn.getOutputStream();
                os.write(data);
                os.close();
                if (conn.getResponseCode() == HTTP_CODE_OK) {
                    response = WechatTool.readBytes(conn.getInputStream());
                }
            } finally {
                conn.disconnect();
            }
            return response;
        } catch (Exception e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 打包基础消息对象
     *
     * @param cmd      GRPC接口ID
     * @param payloads 内容数据字节
     * @return 打包对象结果
     */
    private BaseMsg packBaseMsg(int cmd, byte[] payloads) {
        BaseMsg.Builder builder = BaseMsg.newBuilder();
        builder.setCmd(cmd);
        if (shortServer.get() != null) {
            builder.setShortHost(shortServer.get());
        }
        if (longServer.get() != null) {
            builder.setLongHost(longServer.get());
        }
        builder.setUser(packUser(cmd));
        if (cmd == ApiCmd.CMD_CHECK_LOGIN_QRCODE) {
            if (uuid.get() != null) {
                payloads = uuid.get().getBytes(WechatConst.CHARSET);
            }
            if (longHead.get() != null) {
                builder.setLongHead(ByteString.copyFrom(longHead.get()));
            }
        }
        if (payloads != null) {
            builder.setPayloads(ByteString.copyFrom(payloads));
        }
        return builder.build();
    }

    /**
     * 打包用户数据对象，用于packBaseMsg
     *
     * @param cmd GRPC接口ID
     * @return 打包对象结果
     */
    private User packUser(int cmd) {
        if (userBuilder.get() == null) {
            byte[] sessionKey = WechatTool.hexToBytes(SESSION_KEY);
            User.Builder builder = User.newBuilder()
                    .setSessionKey(ByteString.copyFrom(sessionKey))
                    .setDeviceId(genDeviceId())
                    .setDeviceName(genDeviceName())
                    .setDeviceType(genSoftType());
            userBuilder.set(builder);
        }
        if (cmd == ApiCmd.CMD_CHECK_LOGIN_QRCODE) {
            userBuilder.get().setMaxSyncKey(ByteString.copyFrom(notifyKey.get()));
        }
        return userBuilder.get().build();
    }

    /**
     * 回调接口类
     */
    public static abstract class Callback {
        public void syncMessage(WechatIns ins, WechatObj.Message msg) {
            int msgType = msg.MsgType;
            if (msgType == 1) {
                //文本信息
            } else if (msgType == 2) {
                //通讯录消息
            } else if (msgType == 3) {
                //图片信息
            } else if (msgType == 4) {
                //删除的联系人
            } else if (msgType == 34) {
                //语音信息
            } else if (msgType == 35) {
                //头像信息
            } else if (msgType == 43) {
                //视频信息
            } else if (msgType == 101) {
                //登录的微信号信息
            } else if (msgType == 1491) {
                //群收款消息
            } else if (msgType == 2490) {
                //收到转账消息
            } else if (msgType == 2491) {
                //红包消息
            } else if (msgType == 4901) {
                //App纯文本消息
            } else if (msgType == 4903) {
                //音乐消息
            } else if (msgType == 4905) {
                //普通APP消息
            } else if (msgType == 4906) {
                //文件分享
            } else if (msgType == 4916) {
                //卡卷消息
            } else if (msgType == 4917) {
                //实时位置共享
            } else if (msgType == 4919) {
                //聊天记录分享
            } else if (msgType == 4936) {
                //小程序分享
            } else if (msgType == 10101) {
                //加入了群聊
            } else if (msgType == 10102) {
                //修改群名
            } else if (msgType == 10103) {
                //修改群名
            } else if (msgType == 10104) {
                //成为新群主
            } else if (msgType == 10111) {
                //启用进群方式
            } else if (msgType == 10112) {
                //主已恢复默认进群方式
            }
        }

        public void online(WechatIns ins) {

        }

        public void offline(WechatIns ins) {

        }
    }

    /**
     * 消息队列类
     */
    public static class MessageBox {

        private static final AtomicLong counter = new AtomicLong(0);

        private final AtomicReference<String> id;//处理线程ID
        private final LinkedList<WechatObj.Message> queue;//消息队列

        public MessageBox() {
            this.id = new AtomicReference<>(null);
            this.queue = new LinkedList<>();
        }

        public void push(final WechatIns ins, final WechatObj.Message message) {
            if (id.get() == null) {
                String name = "MessageBox-" + counter.incrementAndGet();
                final String current = WechatTool.randomUUID();
                id.set(current);
                new Thread(() -> {
                    ArrayList<WechatObj.Message> list = new ArrayList<>();
                    while (current.equals(id.get())) {
                        synchronized (queue) {
                            while (!queue.isEmpty()) {
                                list.add(queue.removeFirst());
                            }
                        }
                        for (WechatObj.Message msg : list) {
                            try {
                                ins.callback.syncMessage(ins, msg);
                            } catch (Exception e) {
                                WechatDebug.echo(e);
                            }
                        }
                        list.clear();
                        synchronized (queue) {
                            if (queue.isEmpty()) {
                                try {
                                    queue.wait(5000l);//等待新消息
                                } catch (InterruptedException e) {
                                    WechatDebug.echo(e);
                                }
                            }
                        }
                    }
                    id.compareAndSet(current, null);
                }, name).start();
            }
            synchronized (queue) {
                queue.addLast(message);
                queue.notifyAll();
            }
        }

        public void dispose() {
            id.set(null);
            synchronized (queue) {
                queue.clear();
                queue.notifyAll();
            }
        }

    }

}
