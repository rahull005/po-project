package com.example.po.PoApplication.pocase.dto;

import java.math.BigDecimal;

public record FlexPORequest(

        String requestId,

        String caseId,

        String command,

        String debitAccount,

        BigDecimal amount,

        String currency
) {
}