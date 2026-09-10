package com.example.po.PoApplication.pocase.service;

import com.example.po.PoApplication.pocase.dto.ApprovalActionResponse;
import com.example.po.PoApplication.pocase.dto.RepairPORequest;
import com.example.po.PoApplication.pocase.entity.*;
import com.example.po.PoApplication.pocase.repository.POApprovalRepository;
import com.example.po.PoApplication.pocase.repository.POCaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PORepairService {
    private final POCaseRepository poCaseRepository;
    private final POApprovalRepository poApprovalRepository;
    private final POAuditService auditService;

    public PORepairService(POCaseRepository poCaseRepository,POApprovalRepository approvalRepository,POAuditService auditService){
        this.poCaseRepository = poCaseRepository;
        this.poApprovalRepository = approvalRepository;
        this.auditService = auditService;
    }

    public ApprovalActionResponse repairAndResubmit(String caseId, String userId, RepairPORequest request){
        POCase poCase =
                poCaseRepository.findByCaseId(caseId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "PO case not found: " + caseId
                                ));

        if (poCase.getStatus() != POStatus.REPAIR) {
            throw new IllegalStateException(
                    "PO case is not in REPAIR status"
            );
        }

        //audit-purpose
        POStatus oldStatus = poCase.getStatus();

        poCase.setPurpose(request.purpose());
        poCase.setAmount(request.amount());
        poCase.setCurrency(
                request.currency().toUpperCase()
        );
        poCase.setDebitAccount(request.debitAccount());

        poCase.setStatus(POStatus.PENDING_CHECKER);
        poCase.setUpdatedAt(LocalDateTime.now());

        poCaseRepository.save(poCase);

        POApproval approval = new POApproval();

        approval.setPoCase(poCase);
        approval.setMakerId(userId);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setCreatedAt(LocalDateTime.now());

        poApprovalRepository.save(approval);

        auditService.record(
                poCase,
                AuditAction.RESUBMITTED,
                oldStatus,
                POStatus.PENDING_CHECKER,
                userId,
                request.remarks()
        );

        return new ApprovalActionResponse(
                caseId,
                POStatus.PENDING_CHECKER,
                "PO case repaired and resubmitted for Checker approval"
        );

    }
}
