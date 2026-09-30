package com.example.fakeflex.service;

import com.example.fakeflex.dto.FakeFlexPORequest;
import com.example.fakeflex.dto.FakeFlexPOResponse;
import com.example.fakeflex.dto.FakeFlexScenario;
import com.example.fakeflex.exception.FakeFlexDuplicateRequestException;
import com.example.fakeflex.exception.FakeFlexTimeoutException;
import com.example.fakeflex.idempotency.FakeFlexIdempotencyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeFlexService {

    private static final Logger log =
            LoggerFactory.getLogger(FakeFlexService.class);

    private final FakeFlexIdempotencyStore store;

    public FakeFlexService(FakeFlexIdempotencyStore store) {
        this.store = store;
    }

    public FakeFlexPOResponse createPayOrder(
            String idempotencyKey,
            FakeFlexPORequest request,
            FakeFlexScenario scenario) {

        log.info(
                "Fake Flex request received caseId={} requestId={} idempotencyKey={} scenario={}",
                request.caseId(),
                request.requestId(),
                idempotencyKey,
                scenario
        );

        FakeFlexPOResponse existing =
                store.get(idempotencyKey);

        if (existing != null) {
            log.info(
                    "Fake Flex idempotent replay caseId={} requestId={} poNumber={}",
                    request.caseId(),
                    request.requestId(),
                    existing.poNumber()
            );
            return new FakeFlexPOResponse(
                    existing.requestId(),
                    existing.caseId(),
                    "SUCCESS",
                    existing.poNumber(),
                    existing.amount(),
                    existing.currency(),
                    existing.errorCode(),
                    "Existing Flex result returned for idempotent replay"
            );
        }

        return switch (scenario) {
            case BUSINESS_FAILURE ->
                    businessFailure(request);

            case SYSTEM_ERROR ->
                    throw new IllegalStateException(
                            "Simulated Flex system error"
                    );

            case TIMEOUT ->
                    createThenDelay(
                            idempotencyKey,
                            request
                    );

            case DUPLICATE_REQUEST ->
                    throw new FakeFlexDuplicateRequestException(
                            "Simulated duplicate request"
                    );

            case SUCCESS ->
                    createSuccessfulResponse(
                            idempotencyKey,
                            request,
                            "Pay order created successfully"
                    );
        };
    }

    public FakeFlexPOResponse getPayOrder(String requestId) {
        return storeValueByRequestId(requestId);
    }

    private FakeFlexPOResponse businessFailure(
            FakeFlexPORequest request) {
        return new FakeFlexPOResponse(
                request.requestId(),
                request.caseId(),
                "BUSINESS_FAILURE",
                null,
                request.amount(),
                request.currency(),
                "FLEX-ACCT-001",
                "Debit account is not eligible"
        );
    }

    private FakeFlexPOResponse createThenDelay(
            String idempotencyKey,
            FakeFlexPORequest request) {

        FakeFlexPOResponse response =
                createSuccessfulResponse(
                        idempotencyKey,
                        request,
                        "Pay order created; simulated delayed response"
                );

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Fake Flex delay interrupted",
                    e
            );
        }

        throw new FakeFlexTimeoutException(
                "Simulated delayed Flex response"
        );
    }

    private FakeFlexPOResponse createSuccessfulResponse(
            String idempotencyKey,
            FakeFlexPORequest request,
            String message) {

        FakeFlexPOResponse response =
                new FakeFlexPOResponse(
                        request.requestId(),
                        request.caseId(),
                        "SUCCESS",
                        generatePoNumber(),
                        request.amount(),
                        request.currency(),
                        null,
                        message
                );

        FakeFlexPOResponse existing =
                store.putIfAbsent(
                        idempotencyKey,
                        response
                );

        FakeFlexPOResponse effective =
                existing != null ? existing : response;

        if (existing == null) {
            log.info(
                    "Fake Flex PO created caseId={} requestId={} poNumber={}",
                    request.caseId(),
                    request.requestId(),
                    effective.poNumber()
            );
        }

        return effective;
    }

    private String generatePoNumber() {
        return "FLEXPO-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    private FakeFlexPOResponse storeValueByRequestId(
            String requestId) {

        return store.findByRequestId(requestId)
                .orElseGet(() -> new FakeFlexPOResponse(
                        requestId,
                        null,
                        "NOT_FOUND",
                        null,
                        null,
                        null,
                        "FLEX-NOT-FOUND",
                        "No Flex transaction found"
                ));
    }
}
