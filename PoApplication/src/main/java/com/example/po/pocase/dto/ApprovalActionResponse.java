package com.example.po.pocase.dto;

import com.example.po.pocase.entity.POStatus;

public record ApprovalActionResponse(
        String caseId,
        POStatus status,
        String message
) {
}
