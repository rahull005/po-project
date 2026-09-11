package com.example.po.PoApplication.pocase.dto;

import java.math.BigDecimal;

public record FlexPOResponse(

        String requestId,

        String caseId,

        String status,

        String poNumber,

        BigDecimal amount,

        String currency,

        String message
) {
}
