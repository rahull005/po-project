package com.example.po.PoApplication.pocase.controller;

import com.example.po.PoApplication.pocase.dto.ApprovalActionRequest;
import com.example.po.PoApplication.pocase.dto.ApprovalActionResponse;
import com.example.po.PoApplication.pocase.service.POApprovalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pay-orders")
public class POApprovalController {

    private final POApprovalService approvalService;

    public POApprovalController(POApprovalService approvalService){
        this.approvalService = approvalService;
    }

    @PostMapping("/{caseId}/approve")
    public ApprovalActionResponse approve(
            @PathVariable String caseId,
            @RequestHeader("X-USER-ID") String checkerId,
            @RequestBody ApprovalActionRequest request
            ){
        return approvalService.approve(caseId,checkerId,request);
    }

    @PostMapping("/{caseId}/reject")
    public ApprovalActionResponse reject(
            @PathVariable String caseId,
            @RequestHeader("X-USER-ID") String checkerId,
            @Valid @RequestBody ApprovalActionRequest request) {

        return approvalService.reject(
                caseId,
                checkerId,
                request
        );
    }
}
