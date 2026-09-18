package com.example.po.flex.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
public class FlexConfig {

    @Bean
    public WebClient flexWebClient(
            @Value("${integration.flex.base-url}") String baseUrl,
            @Value("${integration.flex.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${integration.flex.read-timeout-ms}") int readTimeoutMs
            ) {

        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                connectTimeoutMs
                        )
                        .responseTimeout(
                                Duration.ofMillis(
                                        readTimeoutMs
                                )
                        );

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(
                       new ReactorClientHttpConnector(httpClient)
                )
                .build();
    }
}
