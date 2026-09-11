package com.example.po.PoApplication.flex.client;

import com.example.po.PoApplication.pocase.dto.FlexPORequest;
import com.example.po.PoApplication.pocase.dto.FlexPOResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FlexClient {

    private final WebClient webClient;

    public FlexClient(WebClient webClient){
        this.webClient = webClient;
    }

    public FlexPOResponse createPayOrder(FlexPORequest request){
        return webClient
                .post()
                .uri("/fake-flex/api/v1/pay-orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FlexPOResponse.class)
                .block();

    }
}
