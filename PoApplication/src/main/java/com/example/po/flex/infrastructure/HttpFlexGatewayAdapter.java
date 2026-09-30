package com.example.po.flex.infrastructure;

import com.example.po.flex.domain.FlexCreatePORequest;
import com.example.po.flex.domain.FlexCreatePOResponse;
import com.example.po.flex.infrastructure.exception.FlexBusinessException;
import com.example.po.flex.infrastructure.exception.FlexSystemException;
import com.example.po.flex.infrastructure.exception.FlexTimeoutException;
import com.example.po.flex.port.FlexGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class HttpFlexGatewayAdapter implements FlexGateway {

    private static final Logger log =
            LoggerFactory.getLogger(HttpFlexGatewayAdapter.class);

    private static final String CORRELATION_ID = "correlationId";
    private final WebClient webClient;

    public HttpFlexGatewayAdapter(WebClient flexWebClient) {
        this.webClient = flexWebClient;
    }

    @Override
    public FlexCreatePOResponse createPayOrder(FlexCreatePORequest request) {

        log.info(
                "Calling Flex create PO caseId={} requestId={} idempotencyKey={}",
                request.caseId(),
                request.requestId(),
                request.idempotencyKey()
        );

        try {
            FlexCreatePOResponse response = webClient
                    .post()
                    .uri("/fake-flex/api/v1/pay-orders")
                    .header("X-Correlation-Id", correlationId())
                    .header("X-Idempotency-Key", request.idempotencyKey())
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.value() >= 400 && status.value() < 500,
                            clientResponse -> clientResponse
                                    .bodyToMono(String.class)
                                    .defaultIfEmpty("Flex business error")
                                    .flatMap(body -> Mono.error(
                                            new FlexBusinessException(
                                                    String.valueOf(clientResponse.statusCode().value()),
                                                    body
                                            )
                                    )
                            )
                    )
                    .onStatus(
                            status -> status.value() >= 500,
                            clientResponse -> clientResponse
                                    .bodyToMono(String.class)
                                    .defaultIfEmpty("Flex system error")
                                    .flatMap(body -> Mono.error(
                                            new FlexSystemException(
                                                    "Flex HTTP 5xx: " + body,
                                                    null
                                            )
                                    )
                            )
                    )
                    .bodyToMono(FlexCreatePOResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) {
                throw new FlexSystemException("Flex returned an empty response", null);
            }

            return response;

        } catch (FlexBusinessException | FlexSystemException | FlexTimeoutException e) {
            throw e;
        } catch (Exception e) {
            if (isTimeout(e)) {
                throw new FlexTimeoutException("Flex request timed out", e);
            }

            throw new FlexSystemException("Flex communication failed", e);
        }
    }

    @Override
    public FlexCreatePOResponse getPayOrderStatus(String requestId) {

        log.info("Calling Flex status inquiry requestId={}", requestId);

        try {
            FlexCreatePOResponse response = webClient
                    .get()
                    .uri("/fake-flex/api/v1/pay-orders/{requestId}", requestId)
                    .header("X-Correlation-Id", correlationId())
                    .retrieve()
                    .onStatus(
                            status -> status.value() >= 500,
                            clientResponse -> clientResponse
                                    .bodyToMono(String.class)
                                    .defaultIfEmpty("Flex status inquiry failed")
                                    .flatMap(body -> Mono.error(
                                            new FlexSystemException(body, null)
                                    )
                            )
                    )
                    .bodyToMono(FlexCreatePOResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) {
                throw new FlexSystemException(
                        "Flex status inquiry returned an empty response",
                        null
                );
            }

            return response;

        } catch (FlexSystemException | FlexTimeoutException e) {
            throw e;
        } catch (Exception e) {
            if (isTimeout(e)) {
                throw new FlexTimeoutException(
                        "Flex status inquiry timed out",
                        e
                );
            }

            throw new FlexSystemException(
                    "Flex status inquiry failed",
                    e
            );
        }
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            String name = current.getClass().getName();
            if (name.contains("Timeout") || name.contains("ReadTimeout")) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }

    private String correlationId() {
        String value = MDC.get(CORRELATION_ID);
        return value != null ? value : "SYSTEM";
    }
}
