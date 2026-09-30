package com.example.po.pocase.service;

import com.example.po.flex.domain.FlexCreatePORequest;
import com.example.po.flex.domain.FlexCreatePOResponse;
import com.example.po.flex.domain.FlexStatus;
import com.example.po.flex.infrastructure.exception.FlexBusinessException;
import com.example.po.flex.infrastructure.exception.FlexSystemException;
import com.example.po.flex.infrastructure.exception.FlexTimeoutException;
import com.example.po.flex.port.FlexGateway;
import com.example.po.integration.entity.IntegrationTransaction;
import com.example.po.integration.service.IntegrationTransactionService;
import com.example.po.pocase.entity.POCase;
import com.example.po.pocase.entity.POStatus;
import com.example.po.pocase.repository.POCaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class POProcessingService {

    private static final Logger log =
            LoggerFactory.getLogger(POProcessingService.class);

    private final POCaseRepository poCaseRepository;
    private final FlexGateway flexGateway;
    private final POProcessingStateService stateService;
    private final IntegrationTransactionService integrationTransactionService;

    public POProcessingService(
            POCaseRepository poCaseRepository,
            FlexGateway flexGateway,
            POProcessingStateService stateService,
            IntegrationTransactionService integrationTransactionService) {
        this.poCaseRepository = poCaseRepository;
        this.flexGateway = flexGateway;
        this.stateService = stateService;
        this.integrationTransactionService = integrationTransactionService;
    }

    public void processWithFlex(String caseId, Long approvalId) {

        POCase poCase = poCaseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "PO case not found: " + caseId
                ));

        if (poCase.getStatus() == POStatus.PO_CREATED) {
            log.info(
                    "Flex processing already completed caseId={} poNumber={}",
                    caseId,
                    poCase.getPoNumber()
            );
            return;
        }

        if (poCase.getStatus() != POStatus.APPROVED &&
                poCase.getStatus() != POStatus.FLEX_PROCESSING) {
            throw new IllegalStateException(
                    "Only APPROVED or FLEX_PROCESSING cases can be sent to Flex. "
                            + "Current status=" + poCase.getStatus()
            );
        }

        String idempotencyKey =
                caseId + ":FLEX:CREATE_PO:APPROVAL:" + approvalId;

        String requestId = poCase.getFlexRequestId();
        if (requestId == null || !requestId.endsWith("-" + approvalId)) {
            requestId = "FLEX-" + caseId + "-" + approvalId;
        }

        IntegrationTransaction integrationTransaction =
                integrationTransactionService.startOrGet(
                        caseId,
                        requestId,
                        idempotencyKey
                );

        stateService.markFlexProcessing(caseId, requestId);

        log.info(
                "Starting Flex processing caseId={} requestId={} idempotencyKey={} integrationStatus={}",
                caseId,
                requestId,
                idempotencyKey,
                integrationTransaction.getStatus()
        );

        FlexCreatePORequest request = new FlexCreatePORequest(
                requestId,
                idempotencyKey,
                poCase.getCaseId(),
                determineCommand(poCase),
                poCase.getDebitAccount(),
                poCase.getAmount(),
                poCase.getCurrency()
        );

        try {
            FlexCreatePOResponse response =
                    flexGateway.createPayOrder(request);

            if (response.status() == FlexStatus.SUCCESS ||
                    response.status() == FlexStatus.DUPLICATE_REQUEST) {

                if (response.poNumber() == null ||
                        response.poNumber().isBlank()) {
                    throw new FlexSystemException(
                            "Flex returned success without a PO number",
                            null
                    );
                }

                integrationTransactionService.markSuccess(
                        integrationTransaction.getId()
                );

                stateService.markPOCreated(
                        caseId,
                        response.poNumber()
                );

                log.info(
                        "Flex processing completed caseId={} requestId={} poNumber={}",
                        caseId,
                        requestId,
                        response.poNumber()
                );
                return;
            }

            if (response.status() == FlexStatus.BUSINESS_FAILURE) {
                integrationTransactionService.markBusinessFailure(
                        integrationTransaction.getId(),
                        response.errorCode(),
                        response.message()
                );

                stateService.moveToRepair(
                        caseId,
                        response.errorCode(),
                        response.message()
                );
                return;
            }

            throw new FlexSystemException(
                    "Unexpected Flex response status=" + response.status(),
                    null
            );

        } catch (FlexBusinessException e) {
            integrationTransactionService.markBusinessFailure(
                    integrationTransaction.getId(),
                    e.getErrorCode(),
                    e.getMessage()
            );

            stateService.moveToRepair(
                    caseId,
                    e.getErrorCode(),
                    e.getMessage()
            );

        } catch (FlexTimeoutException e) {
            integrationTransactionService.markTimeout(
                    integrationTransaction.getId(),
                    e.getMessage()
            );
            throw e;

        } catch (FlexSystemException e) {
            integrationTransactionService.markSystemFailure(
                    integrationTransaction.getId(),
                    e.getMessage()
            );
            throw e;
        }
    }

    private String determineCommand(POCase poCase) {
        // Temporary development rule. Replace with the approved
        // debit-account-category rule when that business rule is available.
        return "1010";
    }
}
