package com.wxipad;

import com.wxipad.client.CalculationService;
import com.wxipad.web.ApplicationRunnerImpl;
import com.wxipad.wechat.WechatConst;
import com.wxipad.wechat.WechatObj;
import com.wxipad.wechat.WechatTool;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

import static com.wxipad.wechat.tools.uitls.WechatUtil.getDirectory;
import static com.wxipad.wechat.tools.uitls.WechatUtil.getResources;

/**
 * 功能描述
 *
 * @author: aweie
 * @date: 2019/7/4 000420:40
 */
@Data
@Slf4j
@Component
@SpringBootApplication
public class IpadApplication extends SpringBootServletInitializer {

    private static final Logger log = Logger.getLogger(IpadApplication.class.getName());

    public static final String PATH = getDirectory() + File.separator;
    public static Properties properties = new Properties();
    public static String config;

    public static int serverport = 0;
    public static void main(String[] args) throws IOException {
        FileOutputStream oFile = null;
        InputStream in = IpadApplication.class.getResourceAsStream("/application.properties");
        properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        int usedServerport = Integer.parseInt(properties.getProperty("server.port"));
        serverport = usedServerport;
        String UpdateContent = "初始化服务完成!";
        in.close();
        if (args.length == 1) {
            serverport = Integer.parseInt(args[0]);
        } else if (args.length == 0) {
            config = WechatTool.readFile(PATH + "config.json", WechatConst.CHARSET);
            if (config != null && !config.isEmpty()) {
                WechatObj.ConfigBean configBean = GsonUtil.GsonToBean(config, WechatObj.ConfigBean.class);
                serverport = configBean.serverport;
            } else {
                serverport = usedServerport;
            }
        }
        int randomInt = (int) (1000 + Math.random() * (2000 - 1000 + 1));
        if (serverport > 60000) {
            serverport = 60000 - randomInt;
        } else if (serverport < 1000) {
            serverport = 1000 + randomInt;
        }
        if (serverport != usedServerport) {
            String profilepath = getResources() + "application.properties";
            OutputStream fos = new FileOutputStream(profilepath);
            UpdateContent = "Configuration file updated!\n#Update content:\n#[server.port=" + usedServerport + "]  ==>  [server.port=" + serverport + "]";
            properties.setProperty("server.port", "" + serverport);
            properties.store(new OutputStreamWriter(fos, StandardCharsets.UTF_8), UpdateContent);
            fos.close();
        }
        args.equals(serverport);
        SpringApplication.run(IpadApplication.class, args);
        log.info(UpdateContent);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApplicationRunnerImpl.class);
    }
}
