package com.example.po.pocase.service;

import com.example.po.pocase.entity.POCase;
import com.example.po.pocase.entity.POStatus;
import com.example.po.pocase.repository.POCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class POProcessingStateService {

    private final POCaseRepository poCaseRepository;
    private final POAuditService auditService;

    public POProcessingStateService(
            POCaseRepository poCaseRepository,
            POAuditService auditService
            ) {

        this.poCaseRepository = poCaseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void markFlexProcessing(
            String caseId) {

        POCase poCase =
                poCaseRepository.findByCaseId(caseId)
                        .orElseThrow(() -> new IllegalStateException("No Cases found"));

        if (poCase.getStatus()
                != POStatus.APPROVED) {

            throw new IllegalStateException(
                    "PO case is not APPROVED"
            );
        }

        POStatus oldStatus =
                poCase.getStatus();

        poCase.setStatus(
                POStatus.FLEX_PROCESSING
        );

        poCase.setUpdatedAt(
                LocalDateTime.now()
        );

        poCaseRepository.save(poCase);

        auditService.record(
                poCase,
                null,
                oldStatus,
                POStatus.FLEX_PROCESSING,
                "SYSTEM",
                "Case submitted for Flex processing"
        );
    }

    @Transactional
    public void markPOCreated(
            POCase poCase,
            String poNumber) {

        poCase.setPoNumber(poNumber);

        poCase.setStatus(
                POStatus.PO_CREATED
        );

        poCase.setUpdatedAt(
                LocalDateTime.now()
        );

        poCaseRepository.save(poCase);
    }
}