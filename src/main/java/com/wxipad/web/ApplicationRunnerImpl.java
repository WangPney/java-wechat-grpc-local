package com.wxipad.web;

import com.wxipad.IpadApplication;
import com.wxipad.client.CalculationService;
import com.wxipad.wechat.tools.uitls.WechatUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.wxipad.wechat.WechatExample.redisk_key_loinged_user;

@Data
@Slf4j
@Component
@SpringBootApplication
public class ApplicationRunnerImpl implements ApplicationRunner {
	private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
	public static final String PATH = System.getProperty("user.dir") + "/";//当前路径
	public static Properties properties;
	public static byte[] redisId;
	public static int serverPort;
    public static int grpcServerPort;
	public static String serverIp;
	public static String serverHost;
	public static String serverId;
	public static String config;
	public static String CONFIG = System.getProperty("user.dir") + File.separator + "config.json";
	@Override
	public void run(ApplicationArguments args) throws Exception {

		properties = IpadApplication.properties;
		config = IpadApplication.config;
		serverIp = WechatUtil.getRealIp();
		serverPort = Integer.parseInt(properties.getProperty("server.port"));
		grpcServerPort = Integer.parseInt(properties.getProperty("grpcserverport"));
		serverHost = serverIp + ":" + serverPort;
		serverId = WechatUtil.getMd5(serverHost);
		redisId = (redisk_key_loinged_user + serverId).getBytes();
		CalculationService service = new CalculationService();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				//GrpcClient.getInstance().destroy();
				log.info("\n-----------[ WxiPad™ ] 工作已停止 -----------");
			}
		});
		log.info("\n-----------[ WxiPad™ ] 正在工作中 -----------\n地址编号:\t{}\t\n" +
				"本机地址:\thttp://localhost:{}\t\n内网地址:\thttp://127.0.0.1:{}\t\n" +
                        "外网地址:\thttp://{}\t\n监听地址:\thttp://0.0.0.0:{}\t",
                serverId, serverPort, serverPort, serverHost, grpcServerPort);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		service.start(grpcServerPort);
		service.blockUnitShutdown();
	}
}