package com.example.po.PoApplication.flex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FlexConfig {

    @Value("${integration.flex.base-url}")
    private static String baseUrl;

    @Bean
    public WebClient flexWebClient(
            @Value("${integration.flex.base-url}") String baseUrl) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
