package com.example.po.flex.domain;

import java.math.BigDecimal;

public record FlexCreatePORequest(
        String requestId,
        String idempotencyKey,
        String caseId,
        String command,
        String debitAccount,
        BigDecimal amount,
        String currency
) {
}
