package com.example.po.PoApplication.pocase.service;

import com.example.po.PoApplication.flex.client.FlexClient;
import com.example.po.PoApplication.pocase.dto.FlexPORequest;
import com.example.po.PoApplication.pocase.dto.FlexPOResponse;
import com.example.po.PoApplication.pocase.entity.POCase;
import com.example.po.PoApplication.pocase.entity.POStatus;
import com.example.po.PoApplication.pocase.repository.POCaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class POProcessingService {
    private final POCaseRepository poCaseRepository;
    private final FlexClient flexClient;
    private final POAuditService auditService;

    public POProcessingService(
            POCaseRepository poCaseRepository,
            FlexClient flexClient,
            POAuditService auditService
    ){
        this.poCaseRepository = poCaseRepository;
        this.auditService = auditService;
        this.flexClient = flexClient;
    }

    public void processWithFlex(String caseId){
        POCase poCase = poCaseRepository.findByCaseId(caseId)
                .orElseThrow(()->new EntityNotFoundException("No Case found with the following Id "+caseId));

        if(poCase.getStatus() != POStatus.APPROVED){
            throw new IllegalStateException(
                    "Only APPROVED cases can be sent to Flex"
            );
        }

        String flexRequestId = generateFlexRequestId();

        POStatus oldStatus = poCase.getStatus();

        poCase.setFlexRequestId(flexRequestId);
        poCase.setStatus(POStatus.FLEX_PROCESSING);

        poCaseRepository.save(poCase);

        auditService.record(
                poCase,
                null,
                oldStatus,
                POStatus.FLEX_PROCESSING,
                "SYSTEM",
                "Request sent to Flex"
        );

        FlexPORequest flexPORequest = new FlexPORequest(
                flexRequestId,
                poCase.getCaseId(),
                determineCommand(poCase),
                poCase.getDebitAccount(),
                poCase.getAmount(),
                poCase.getCurrency()
        );

        FlexPOResponse response =
                flexClient.createPayOrder(flexPORequest);

        if (!"SUCCESS".equals(response.status())) {

            throw new IllegalStateException(
                    "Flex processing failed: "
                            + response.message()
            );
        }

        poCase.setPoNumber(response.poNumber());
        poCase.setStatus(POStatus.PO_CREATED);

        poCaseRepository.save(poCase);

        auditService.record(
                poCase,
                null,
                POStatus.FLEX_PROCESSING,
                POStatus.PO_CREATED,
                "SYSTEM",
                "PO successfully created in Flex: "
                        + response.poNumber()
        );

    }

    private String determineCommand(POCase poCase) {
        /*
         * Temporary rule.
         *
         * Actual command selection will be based
         * on debit-account category/business rules.
         */

        return "1010";
    }

    private String generateFlexRequestId() {
        return UUID.randomUUID()
                .toString()
                .replace("-","")
                .substring(0,16)
                .toUpperCase();
    }


}
