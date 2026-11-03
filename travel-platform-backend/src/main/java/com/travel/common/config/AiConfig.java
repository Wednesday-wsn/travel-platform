package com.travel.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 大模型调用配置：RestTemplate（带超时）
 */
@Configuration
public class AiConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        // 大模型生成较慢，读超时设 60 秒
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
    }
}
