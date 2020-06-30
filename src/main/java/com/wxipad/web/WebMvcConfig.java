package com.wxipad.web;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * mvc 配置
 *
 * @ClassName: WebMvcConfig
 */
@Component
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	private transient static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(0, fastJsonHttpMessageConverter());
	}

	@Bean
	public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
		FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<>();
		supportedMediaTypes.add(new MediaType("application", "json", Charset.forName("UTF-8")));
		fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
		fastJsonHttpMessageConverter.setFeatures(SerializerFeature.WriteDateUseDateFormat);
		return fastJsonHttpMessageConverter;
	}
}
