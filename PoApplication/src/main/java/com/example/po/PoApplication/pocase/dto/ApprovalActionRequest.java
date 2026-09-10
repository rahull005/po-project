package com.example.po.PoApplication.pocase.dto;

import jakarta.validation.constraints.Size;

public record ApprovalActionRequest(

        @Size(max = 1000)
        String comments
) {
}
