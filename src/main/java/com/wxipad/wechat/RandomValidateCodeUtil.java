package com.wxipad.wechat;

import com.wxipad.wechat.tools.uitls.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import static com.wxipad.wechat.tools.uitls.WechatUtil.readFile;

/**
 * 功能描述
 *
 * @author: aweie
 * @date: 2019/7/3 000323:12
 */
public class RandomValidateCodeUtil {
    public static final String RANDOMCODEKEY = "RANDOMVALIDATECODEKEY";//放到session中的key
    private static final Logger logger = LoggerFactory.getLogger(RandomValidateCodeUtil.class);
    private String randString = "0123456789";//随机产生只有数字的字符串 private String
    //private String randString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";//随机产生只有字母的字符串
    //private String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";//随机产生数字与字母组合的字符串
    private int width = 200;// 图片宽
    private int height = 220;// 图片高
    private int lineSize = 40;// 干扰线数量
    private int stringNum = 4;// 随机产生字符数量
    private Random random = new Random();

    /**
     * 获得字体
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.CENTER_BASELINE, 18);
    }

    /**
     * 获得颜色
     */
    private Color getRandColor(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }

    /**
     * 生成随机图片
     */
    public void getRandcode(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();// 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
        g.fillRect(0, 0, width, height);//图片大小
        g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 18));//字体大小
        g.setColor(getRandColor(110, 133));//字体颜色
        //将生成的随机字符串保存到session中
        String sessionKey = WechatUtil.getRandomUUID();
        session.removeAttribute(RANDOMCODEKEY);
        session.setAttribute(RANDOMCODEKEY, sessionKey);
        g.dispose();
        try {
            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(image, "JPEG", response.getOutputStream());
        } catch (Exception e) {
            logger.error("将内存中的图片通过流动形式输出到客户端失败>>>>  ", e);
        }
    }

    /**
     * 生成随机图片
     */
    public void getQrcode(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        byte[] qrcode = readFile(System.getProperty("user.dir") + "/" + "qrcode.png");
        String randomString = WechatExample.randomId;
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        ByteArrayInputStream bis = new ByteArrayInputStream(qrcode);
        BufferedImage img = null;
        try {
            img = ImageIO.read(bis);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedImage image = null;
        //将生成的随机字符串保存到session中
        session.removeAttribute(RANDOMCODEKEY);
        session.setAttribute(RANDOMCODEKEY, randomString);
        try {
            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(img, "JPEG", response.getOutputStream());
        } catch (Exception e) {
            logger.error("将内存中的图片通过流动形式输出到客户端失败>>>>  ", e);
        }

    }

    /**
     * 绘制字符串
     */
    private String drowString(Graphics g, String randomString, int i) {
        g.setFont(getFont());
        g.setColor(new Color(random.nextInt(101), random.nextInt(111), random
                .nextInt(121)));
        String rand = String.valueOf(getRandomString(random.nextInt(randString
                .length())));
        randomString += rand;
        g.translate(random.nextInt(3), random.nextInt(3));
        g.drawString(rand, 13 * i, 16);
        return randomString;
    }

    /**
     * 绘制干扰线
     */
    private void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /**
     * 获取随机的字符
     */
    public String getRandomString(int num) {
        return String.valueOf(randString.charAt(num));
    }
}