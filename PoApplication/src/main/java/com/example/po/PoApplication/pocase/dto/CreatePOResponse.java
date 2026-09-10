package com.example.po.PoApplication.pocase.dto;

import com.example.po.PoApplication.pocase.entity.POStatus;

public record CreatePOResponse(
        String caseId,
        POStatus status,
        String message
) {
}
