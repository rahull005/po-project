package com.example.po.pocase.service;

import com.example.po.pocase.entity.AuditAction;
import com.example.po.pocase.entity.POCase;
import com.example.po.pocase.entity.POStatus;
import com.example.po.pocase.repository.POCaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class POProcessingStateService {

    private final POCaseRepository poCaseRepository;
    private final POAuditService auditService;

    public POProcessingStateService(
            POCaseRepository poCaseRepository,
            POAuditService auditService) {
        this.poCaseRepository = poCaseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void markFlexProcessing(String caseId, String flexRequestId) {

        POCase poCase = findCase(caseId);

        if (poCase.getStatus() == POStatus.PO_CREATED) {
            return;
        }

        if (poCase.getStatus() != POStatus.APPROVED &&
                poCase.getStatus() != POStatus.FLEX_PROCESSING) {
            throw new IllegalStateException(
                    "PO case cannot enter FLEX_PROCESSING from status "
                            + poCase.getStatus()
            );
        }

        POStatus oldStatus = poCase.getStatus();

        if (poCase.getFlexRequestId() == null) {
            poCase.setFlexRequestId(flexRequestId);
        }

        if (poCase.getStatus() != POStatus.FLEX_PROCESSING) {
            poCase.setStatus(POStatus.FLEX_PROCESSING);
            auditService.record(
                    poCase,
                    AuditAction.FLEX_PROCESSING,
                    oldStatus,
                    POStatus.FLEX_PROCESSING,
                    "SYSTEM",
                    "Case submitted for Flex processing"
            );
        }

        poCase.setUpdatedAt(LocalDateTime.now());
        poCaseRepository.save(poCase);
    }

    @Transactional
    public void markPOCreated(String caseId, String poNumber) {

        POCase poCase = findCase(caseId);

        if (poCase.getStatus() == POStatus.PO_CREATED) {
            if (!poNumber.equals(poCase.getPoNumber())) {
                throw new IllegalStateException(
                        "PO case is already created with a different PO number"
                );
            }
            return;
        }

        POStatus oldStatus = poCase.getStatus();

        poCase.setPoNumber(poNumber);
        poCase.setStatus(POStatus.PO_CREATED);
        poCase.setUpdatedAt(LocalDateTime.now());

        poCaseRepository.save(poCase);

        auditService.record(
                poCase,
                AuditAction.PO_CREATED,
                oldStatus,
                POStatus.PO_CREATED,
                "SYSTEM",
                "PO successfully created in Flex: " + poNumber
        );
    }

    @Transactional
    public void moveToRepair(String caseId, String errorCode, String message) {

        POCase poCase = findCase(caseId);

        if (poCase.getStatus() == POStatus.REPAIR) {
            return;
        }

        POStatus oldStatus = poCase.getStatus();

        poCase.setStatus(POStatus.REPAIR);
        poCase.setUpdatedAt(LocalDateTime.now());
        poCaseRepository.save(poCase);

        auditService.record(
                poCase,
                AuditAction.MOVED_TO_REPAIR,
                oldStatus,
                POStatus.REPAIR,
                "SYSTEM",
                "Flex business failure code=" + errorCode
                        + ", message=" + message
        );
    }

    private POCase findCase(String caseId) {
        return poCaseRepository.findByCaseId(caseId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "PO case not found: " + caseId
                        ));
    }
}
