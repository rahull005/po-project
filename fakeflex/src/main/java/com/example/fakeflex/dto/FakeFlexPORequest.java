package com.example.fakeflex.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FakeFlexPORequest(
        @NotBlank
        String requestId,

        @NotBlank
        String caseId,

        @NotBlank
        String command,

        @NotBlank
        String debitAccount,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotBlank
        String currency
        ) {
}
