package com.example.fakeflex.service;

import com.example.fakeflex.dto.FakeFlexPORequest;
import com.example.fakeflex.dto.FakeFlexPOResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeFlexService {
    public FakeFlexPOResponse createPayOrder(
            FakeFlexPORequest request) {

        String poNumber =
                "FLEXPO-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();

        return new FakeFlexPOResponse(
                request.requestId(),
                request.caseId(),
                "SUCCESS",
                poNumber,
                request.amount(),
                request.currency(),
                "Pay order created successfully"
        );
    }
}
