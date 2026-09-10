package com.example.po.PoApplication.pocase.service;

import com.example.po.PoApplication.pocase.dto.ApprovalActionRequest;
import com.example.po.PoApplication.pocase.dto.ApprovalActionResponse;
import com.example.po.PoApplication.pocase.entity.*;
import com.example.po.PoApplication.pocase.repository.POApprovalRepository;
import com.example.po.PoApplication.pocase.repository.POCaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class POApprovalService {

    private final POCaseRepository poCaseRepository;
    private final POApprovalRepository poApprovalRepository;
    private final POAuditService auditService;

    public POApprovalService(POCaseRepository poCaseRepository,POApprovalRepository poApprovalRepository,POAuditService auditService){
        this.poCaseRepository = poCaseRepository;
        this.poApprovalRepository = poApprovalRepository;
        this.auditService = auditService;
    }


    @Transactional
    public ApprovalActionResponse approve(String caseId, String checkerId, ApprovalActionRequest request){
        POCase poCase = getCase(caseId);
        validateCheckerAction(poCase);

        //audit
        POStatus oldStatus = poCase.getStatus();

        POApproval poApproval = getPendingApproval(caseId);
        poApproval.setCheckerId(checkerId);
        poApproval.setStatus(ApprovalStatus.APPROVED);
        poApproval.setComments(request.comments());
        poApproval.setActionAt(LocalDateTime.now());

        poApprovalRepository.save(poApproval);

        poCase.setUpdatedAt(LocalDateTime.now());
        poCase.setStatus(POStatus.APPROVED);
        poCaseRepository.save(poCase);

        //audit
        auditService.record(poCase, AuditAction.APPROVED,oldStatus,POStatus.APPROVED,checkerId,request.comments());


        return new ApprovalActionResponse(
                caseId,POStatus.APPROVED,"Pay order request approved successfully"
        );
    }


    @Transactional
    public ApprovalActionResponse reject(String caseId,String checkerId,ApprovalActionRequest request){
        POCase poCase = getCase(caseId);
        validateCheckerAction(poCase);

        //audit
        POStatus oldStatus = poCase.getStatus();

        POApproval approval = getPendingApproval(caseId);

        approval.setComments(request.comments());
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setActionAt(LocalDateTime.now());
        approval.setCheckerId(checkerId);

        poApprovalRepository.save(approval);

        poCase.setStatus(POStatus.REPAIR);
        poCase.setUpdatedAt(LocalDateTime.now());

        poCaseRepository.save(poCase);

        //audit
        auditService.record(poCase,AuditAction.REJECTED,oldStatus,POStatus.REPAIR,checkerId,request.comments());

        return new ApprovalActionResponse(caseId,POStatus.REPAIR,"Pay order request rejected and moved to repair");
    }


    private POCase getCase(String caseId) {
        return poCaseRepository.findByCaseId(caseId)
                .orElseThrow(()->  new EntityNotFoundException("PO case not found: " + caseId));
    }

    private void validateCheckerAction(POCase poCase)  {
        if(poCase.getStatus() != POStatus.PENDING_CHECKER){
            throw new IllegalStateException("PO case is not pending checker approval");
        }
    }

    private POApproval getPendingApproval(String caseId) {
        return poApprovalRepository.findFirstByPoCaseCaseIdAndStatusOrderByCreatedAtDesc(caseId,ApprovalStatus.PENDING)
                .orElseThrow(()->new IllegalStateException("No cases found"));
    }

}
