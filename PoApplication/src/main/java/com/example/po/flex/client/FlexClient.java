package com.example.po.flex.client;

import com.example.po.common.logging.CorrelationIdFilter;
import com.example.po.pocase.dto.FlexPORequest;
import com.example.po.pocase.dto.FlexPOResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FlexClient {

    private static final Logger log =
            LoggerFactory.getLogger(FlexClient.class);

    private final WebClient webClient;

    public FlexClient(WebClient flexWebClient) {
        this.webClient = flexWebClient;
    }

    public FlexPOResponse createPayOrder(
            FlexPORequest request) {

        log.info(
                "Calling Flex create PO caseId={} requestId={}",
                request.caseId(),
                request.requestId()
        );

        return webClient
                .post()
                .uri("/fake-flex/api/v1/pay-orders")
                .header(
                        "X-Correlation-Id",
                        currentCorrelationId()
                )
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FlexPOResponse.class)
                .block();
    }

    public FlexPOResponse getPayOrder(
            String requestId) {

        log.info(
                "Calling Flex status inquiry requestId={}",
                requestId
        );

        return webClient
                .get()
                .uri(
                        "/fake-flex/api/v1/pay-orders/{requestId}",
                        requestId
                )
                .retrieve()
                .bodyToMono(FlexPOResponse.class)
                .block();
    }

    private String currentCorrelationId() {

        String correlationId =
                MDC.get(
                        CorrelationIdFilter.CORRELATION_ID
                );

        return correlationId != null
                ? correlationId
                : "SYSTEM";
    }
}
