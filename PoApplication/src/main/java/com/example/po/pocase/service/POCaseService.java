package com.example.po.pocase.service;

import com.example.po.pocase.dto.CreatePORequest;
import com.example.po.pocase.dto.CreatePOResponse;
import com.example.po.pocase.entity.ApprovalStatus;
import com.example.po.pocase.entity.AuditAction;
import com.example.po.pocase.entity.POApproval;
import com.example.po.pocase.entity.POCase;
import com.example.po.pocase.entity.POStatus;
import com.example.po.pocase.repository.POApprovalRepository;
import com.example.po.pocase.repository.POCaseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class POCaseService {
    private final POCaseRepository repository;
    private final ApprovalRuleService approvalRuleService;
    private final POApprovalRepository approvalRepository;
    private final POAuditService auditService;

    public POCaseService(
            POCaseRepository repository,
            POApprovalRepository approvalRepository,
            ApprovalRuleService approvalRuleService,
            POAuditService auditService) {

        this.repository = repository;
        this.approvalRepository = approvalRepository;
        this.approvalRuleService = approvalRuleService;
        this.auditService = auditService;
    }

    @Transactional
    public CreatePOResponse create(
            CreatePORequest request,
            String makerId) {

        POCase poCase = new POCase();

        poCase.setCaseId(generateCaseId());

        poCase.setRequestType(request.requestType());
        poCase.setChannel(request.channel());
        poCase.setDepartment(request.department());
        poCase.setPurpose(request.purpose());
        poCase.setAmount(request.amount());
        poCase.setCurrency(request.currency().toUpperCase());
        poCase.setDebitAccount(request.debitAccount());
        poCase.setDeliveryType(request.deliveryType());
        poCase.setPayOrderRequired(request.payOrderRequired());

        poCase.setStatus(POStatus.SUBMITTED);

        LocalDateTime now = LocalDateTime.now();

        poCase.setCreatedAt(now);
        poCase.setUpdatedAt(now);
//        poCase.setVersion(0L);

        repository.save(poCase);

        auditService.record(
                poCase,
                AuditAction.CASE_CREATED,
                null,
                POStatus.SUBMITTED,
                makerId,
                "Pay order request created"
        );

        boolean reviewRequired =
                approvalRuleService.isReviewRequired(poCase);

        if (reviewRequired) {

            POApproval approval = new POApproval();

            approval.setPoCase(poCase);
            approval.setMakerId(makerId);
            approval.setStatus(ApprovalStatus.PENDING);
            approval.setCreatedAt(now);

            approvalRepository.save(approval);

            auditService.record(
                    poCase,
                    AuditAction.SUBMITTED_FOR_APPROVAL,
                    POStatus.SUBMITTED,
                    POStatus.PENDING_CHECKER,
                    makerId,
                    "Case submitted for Checker approval"
            );

            poCase.setStatus(POStatus.PENDING_CHECKER);
            poCase.setUpdatedAt(LocalDateTime.now());

            repository.save(poCase);
        }

        return new CreatePOResponse(
                poCase.getCaseId(),
                poCase.getStatus(),
                "Pay order request created successfully"
        );
    }

    private String generateCaseId() {
        return "PO-" + UUID.randomUUID();
    }
}
