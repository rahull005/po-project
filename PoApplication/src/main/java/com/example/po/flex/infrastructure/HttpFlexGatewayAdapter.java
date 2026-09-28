package com.example.po.flex.infrastructure;

import com.example.po.flex.domain.*;
import com.example.po.flex.infrastructure.exception.*;
import com.example.po.flex.port.FlexGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class HttpFlexGatewayAdapter
        implements FlexGateway {

    private static final Logger log =
            LoggerFactory.getLogger(
                    HttpFlexGatewayAdapter.class
            );

    private final WebClient webClient;

    public HttpFlexGatewayAdapter(
            WebClient flexWebClient) {

        this.webClient = flexWebClient;
    }

    @Override
    public FlexCreatePOResponse createPayOrder(
            FlexCreatePORequest request) {

        log.info(
                "Calling Flex create-pay-order caseId={} requestId={}",
                request.caseId(),
                request.requestId()
        );

        try {

            return webClient
                    .post()
                    .uri(
                            "/fake-flex/api/v1/pay-orders"
                    )
                    .header(
                            "X-Correlation-Id",
                            getCorrelationId()
                    )
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.value() >= 400 &&
                                    status.value() < 500,
                            response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(body ->
                                            Mono.error(
                                                    new FlexBusinessException(
                                                            String.valueOf(
                                                                    response.statusCode()
                                                                            .value()
                                                            ),
                                                            body
                                                    )
                                            )
                                    )
                    )
                    .onStatus(
                            status -> status.value() >= 500,
                            response ->
                                    response.bodyToMono(String.class ).flatMap(
                                            body ->
                                            Mono.error(
                                                    new FlexSystemException(
                                                            "Flex HTTP 5xx: "
                                                                    + body,
                                                            null
                                                    )
                                            )
                                    )
                    )
                    .bodyToMono(
                            FlexCreatePOResponse.class
                    )
                    .timeout(
                            Duration.ofSeconds(10)
                    )
                    .block();

        } catch (FlexBusinessException e) {

            throw e;

        } catch (Exception e) {

            if (isTimeout(e)) {

                throw new FlexTimeoutException(
                        "Flex request timed out",
                        e
                );
            }

            throw new FlexSystemException(
                    "Flex communication failed",
                    e
            );
        }
    }

    @Override
    public FlexCreatePOResponse getPayOrderStatus(
            String requestId) {

        log.info(
                "Calling Flex status inquiry requestId={}",
                requestId
        );

        try {

            return webClient
                    .get()
                    .uri(
                            "/fake-flex/api/v1/pay-orders/{requestId}",
                            requestId
                    )
                    .header(
                            "X-Correlation-Id",
                            getCorrelationId()
                    )
                    .retrieve()
                    .bodyToMono(
                            FlexCreatePOResponse.class
                    )
                    .timeout(
                            Duration.ofSeconds(10)
                    )
                    .block();

        } catch (Exception e) {

            throw new FlexSystemException(
                    "Flex status inquiry failed",
                    e
            );
        }
    }

    private boolean isTimeout(
            Exception e) {

        return e.getClass()
                .getName()
                .contains("Timeout");
    }

    private String getCorrelationId() {

        String value =
                org.slf4j.MDC.get(
                        "correlationId"
                );

        return value != null
                ? value
                : "SYSTEM";
    }

    /*

            Now throughout that request, you might have logs like:

                INFO PaymentService - Payment started
                INFO FakeFlexClient - Calling FakeFlex
                INFO PaymentService - Payment completed

            With MDC configured in your logging pattern, they can automatically become:

                INFO [8f72a91c] Payment started
                INFO [8f72a91c] Calling FakeFlex
                INFO [8f72a91c] Payment completed
     */
}