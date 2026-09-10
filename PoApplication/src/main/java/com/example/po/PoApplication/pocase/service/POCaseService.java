package com.example.po.PoApplication.pocase.service;

import com.example.po.PoApplication.pocase.dto.CreatePORequest;
import com.example.po.PoApplication.pocase.dto.CreatePOResponse;
import com.example.po.PoApplication.pocase.entity.ApprovalStatus;
import com.example.po.PoApplication.pocase.entity.POApproval;
import com.example.po.PoApplication.pocase.entity.POCase;
import com.example.po.PoApplication.pocase.entity.POStatus;
import com.example.po.PoApplication.pocase.repository.POApprovalRepository;
import com.example.po.PoApplication.pocase.repository.POCaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class POCaseService {
    private final POCaseRepository repository;
    private final ApprovalRuleService approvalRuleService;
    private final POApprovalRepository approvalRepository;

    public POCaseService(
            POCaseRepository repository,
            POApprovalRepository approvalRepository,
            ApprovalRuleService approvalRuleService) {

        this.repository = repository;
        this.approvalRepository = approvalRepository;
        this.approvalRuleService = approvalRuleService;
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

        boolean reviewRequired =
                approvalRuleService.isReviewRequired(poCase);

        if (reviewRequired) {

            POApproval approval = new POApproval();

            approval.setPoCase(poCase);
            approval.setMakerId(makerId);
            approval.setStatus(ApprovalStatus.PENDING);
            approval.setCreatedAt(now);

            approvalRepository.save(approval);

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

        return "PO-" +
                LocalDateTime.now()
                        .toString()
                        .replace("-", "")
                        .replace(":", "")
                        .replace(".", "")
                        .substring(0, 15)
                + "-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase();
    }
}
