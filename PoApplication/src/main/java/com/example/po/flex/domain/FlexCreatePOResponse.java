package com.example.po.flex.domain;

import java.math.BigDecimal;

public record FlexCreatePOResponse(
        String requestId,

        String caseId,

        FlexStatus status,

        String poNumber,

        BigDecimal amount,

        String currency,

        String errorCode,

        String message
) {
}
