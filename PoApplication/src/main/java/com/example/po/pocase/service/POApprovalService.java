package com.example.po.pocase.service;

import com.example.po.integration.service.OutboxService;
import com.example.po.pocase.dto.ApprovalActionRequest;
import com.example.po.pocase.dto.ApprovalActionResponse;
import com.example.po.pocase.entity.*;
import com.example.po.pocase.repository.POApprovalRepository;
import com.example.po.pocase.repository.POCaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class POApprovalService {

    private final POCaseRepository poCaseRepository;
    private final POApprovalRepository poApprovalRepository;
    private final POAuditService auditService;
    private final POProcessingService processingService;
    private final OutboxService outboxService;

    //logger
    private static final Logger log = LoggerFactory.getLogger(POApprovalService.class);

    public POApprovalService(
            POCaseRepository poCaseRepository,
            POApprovalRepository poApprovalRepository,
            POAuditService auditService,
            POProcessingService processingService,
            OutboxService outboxService
    ){
        this.poCaseRepository = poCaseRepository;
        this.poApprovalRepository = poApprovalRepository;
        this.auditService = auditService;
        this.processingService = processingService;
        this.outboxService = outboxService;
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
        auditService.record(
                poCase,
                AuditAction.APPROVED,
                oldStatus,
                POStatus.APPROVED,
                checkerId,
                request.comments()
        );


        log.info("Po approval completed with case-Id {}, status-Id {}, checker-id{}",
                caseId,
                checkerId,
                POStatus.APPROVED
        );


        //flex
//        processingService.processWithFlex(caseId);

        //implemented outbox-pattern
        outboxService
                .createPOApprovedEvent(caseId,poApproval.getId());

        log.info(
                "PO approved event created caseId={} approvalId={}",
                caseId,
                poApproval.getId()
        );



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
