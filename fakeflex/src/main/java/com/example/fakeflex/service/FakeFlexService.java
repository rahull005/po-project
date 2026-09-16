package com.example.fakeflex.service;

import com.example.fakeflex.dto.FakeFlexPORequest;
import com.example.fakeflex.dto.FakeFlexPOResponse;
import com.example.fakeflex.dto.FakeFlexScenario;
import com.example.fakeflex.exception.FakeFlexTimeoutException;
import com.example.fakeflex.idempotency.FakeFlexIdempotencyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeFlexService {

    private static final Logger log = LoggerFactory.getLogger(FakeFlexService.class);
    private final FakeFlexIdempotencyStore store;

    public FakeFlexService(FakeFlexIdempotencyStore store){
        this.store = store;
    }

    public FakeFlexPOResponse createPayOrder(
            FakeFlexPORequest request,
            FakeFlexScenario scenario
            ) {


        log.info(
                "Fake Flex request received caseId={} requestId={} scenario={}",
                request.caseId(),
                request.requestId(),
                scenario
        );

        FakeFlexPOResponse existing = store.get(request.requestId());

        if(existing != null){
            return new FakeFlexPOResponse(
                    request.requestId(),
                    request.caseId(),
                    "DUPLICATE_REQUEST",
                    existing.poNumber(),
                    existing.amount(),
                    existing.currency(),
                    "Request already processed"
            );
        }

        switch (scenario) {

            case BUSINESS_FAILURE:
                return new FakeFlexPOResponse(
                        request.requestId(),
                        request.caseId(),
                        "BUSINESS_FAILURE",
                        null,
                        request.amount(),
                        request.currency(),
                        "Debit account is not eligible"
                );

            case SYSTEM_ERROR:
                throw new RuntimeException(
                        "Simulated Flex system error"
                );

            case TIMEOUT:

                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "Fake Flex interrupted",
                            e
                    );
                }

                throw new FakeFlexTimeoutException(
                        "Simulated Flex timeout"
                );

            case SUCCESS:

                String poNumber =
                        "FLEXPO-" +
                                UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase();

                FakeFlexPOResponse response =
                        new FakeFlexPOResponse(
                                request.requestId(),
                                request.caseId(),
                                "SUCCESS",
                                poNumber,
                                request.amount(),
                                request.currency(),
                                "Pay order created successfully"
                        );

                store.put(
                        request.requestId(),
                        response
                );

                log.info(
                        "Fake Flex PO created caseId={} requestId={} poNumber={}",
                        request.caseId(),
                        request.requestId(),
                        poNumber
                );

                return response;

            case DUPLICATE_REQUEST:

                return new FakeFlexPOResponse(
                        request.requestId(),
                        request.caseId(),
                        "DUPLICATE_REQUEST",
                        null,
                        request.amount(),
                        request.currency(),
                        "Duplicate request"
                );

            default:

                throw new IllegalStateException(
                        "Unsupported scenario: " + scenario
                );
        }
    }





    public FakeFlexPOResponse getPayOrder(String requestId) {
        FakeFlexPOResponse response = store.get(requestId);
        if (response == null) {

            return new FakeFlexPOResponse(
                    requestId,
                    null,
                    "NOT_FOUND",
                    null,
                    null,
                    null,
                    "No Flex transaction found"
            );
        }

        return response;
    }
}
