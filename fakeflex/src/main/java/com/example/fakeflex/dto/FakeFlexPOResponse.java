package com.example.fakeflex.dto;

import java.math.BigDecimal;

public record FakeFlexPOResponse(
        String requestId,

        String caseId,

        String status,

        String poNumber,

        BigDecimal amount,

        String currency,

        String message
) {
}
