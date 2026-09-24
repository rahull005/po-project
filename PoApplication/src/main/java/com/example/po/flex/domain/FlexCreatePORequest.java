package com.example.po.flex.domain;

import java.math.BigDecimal;

public record FlexCreatePORequest(
        String requestId,

        String caseId,

        String command,

        String debitAccount,

        BigDecimal amount,

        String currency
) {

}
