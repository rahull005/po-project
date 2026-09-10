package com.example.po.PoApplication.pocase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RepairPORequest(
        @NotBlank String purpose,
        @NotNull @DecimalMin(value = "0.01")BigDecimal amount,
        @NotBlank @Size(min = 3,max = 3) String currency,
        @NotBlank String debitAccount,
        @NotBlank String remarks
        ) {
}
