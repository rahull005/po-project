package com.example.po.pocase.controller;

import com.example.po.pocase.dto.ApprovalActionResponse;
import com.example.po.pocase.dto.RepairPORequest;
import com.example.po.pocase.service.PORepairService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pay-orders")
public class PORepairController {

    private final PORepairService repairService;

    public PORepairController(
            PORepairService repairService) {

        this.repairService = repairService;
    }

    @PostMapping("/{caseId}/repair")
    public ApprovalActionResponse repair(
            @PathVariable String caseId,
            @RequestHeader("X-USER-ID") String userId,
            @Valid @RequestBody RepairPORequest request) {

        return repairService.repairAndResubmit(
                caseId,
                userId,
                request
        );
    }
}
