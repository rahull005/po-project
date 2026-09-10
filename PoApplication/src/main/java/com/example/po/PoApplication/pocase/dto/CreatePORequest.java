package com.example.po.PoApplication.pocase.dto;

import com.example.po.PoApplication.pocase.entity.Channel;
import com.example.po.PoApplication.pocase.entity.DeliveryType;
import com.example.po.PoApplication.pocase.entity.RequestType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePORequest(
        @NotNull
        RequestType requestType,

        @NotNull
        Channel channel,

        @NotBlank
        String department,

        @NotBlank
        String purpose,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotBlank
        String debitAccount,

        @NotNull
        DeliveryType deliveryType,

        @NotNull
        Boolean payOrderRequired
) {
}
