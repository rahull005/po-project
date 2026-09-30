package com.example.po.integration.service;

import com.example.po.integration.entity.IntegrationTransaction;
import com.example.po.integration.repository.IntegrationTransactionRepository;
import com.example.po.integration.entity.IntegrationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IntegrationTransactionService {

    private final IntegrationTransactionRepository repository;

    public IntegrationTransactionService(
            IntegrationTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IntegrationTransaction startOrGet(
            String caseId,
            String requestId,
            String idempotencyKey) {

        return repository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    if (!existing.getCaseId().equals(caseId) ||
                            !existing.getRequestId().equals(requestId)) {
                        throw new IllegalStateException(
                                "Idempotency key is already associated with another integration transaction"
                        );
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();

                    IntegrationTransaction tx = new IntegrationTransaction();
                    tx.setCaseId(caseId);
                    tx.setSystemName("FLEX");
                    tx.setOperation("CREATE_PO");
                    tx.setRequestId(requestId);
                    tx.setIdempotencyKey(idempotencyKey);
                    tx.setStatus(IntegrationStatus.IN_PROGRESS);
                    tx.setRetryCount(0);
                    tx.setCreatedAt(now);
                    tx.setUpdatedAt(now);

                    return repository.save(tx);
                });
    }

    @Transactional
    public void markSuccess(Long transactionId) {
        updateStatus(transactionId, IntegrationStatus.SUCCESS, null, null);
    }

    @Transactional
    public void markBusinessFailure(
            Long transactionId,
            String errorCode,
            String message) {
        updateStatus(
                transactionId,
                IntegrationStatus.BUSINESS_FAILURE,
                errorCode,
                message
        );
    }

    @Transactional
    public void markTimeout(Long transactionId, String message) {
        updateStatus(
                transactionId,
                IntegrationStatus.TIMEOUT,
                "FLEX_TIMEOUT",
                message
        );
    }

    @Transactional
    public void markSystemFailure(Long transactionId, String message) {
        IntegrationTransaction tx = get(transactionId);
        tx.setStatus(IntegrationStatus.SYSTEM_FAILURE);
        tx.setRetryCount(tx.getRetryCount() + 1);
        tx.setErrorCode("FLEX_SYSTEM_ERROR");
        tx.setErrorMessage(message);
        tx.setUpdatedAt(LocalDateTime.now());
        repository.save(tx);
    }

    @Transactional
    public void markUnknown(Long transactionId, String message) {
        updateStatus(
                transactionId,
                IntegrationStatus.UNKNOWN,
                "FLEX_UNKNOWN",
                message
        );
    }

    private void updateStatus(
            Long transactionId,
            IntegrationStatus status,
            String errorCode,
            String errorMessage) {

        IntegrationTransaction tx = get(transactionId);
        tx.setStatus(status);
        tx.setErrorCode(errorCode);
        tx.setErrorMessage(errorMessage);
        tx.setUpdatedAt(LocalDateTime.now());

        if (status == IntegrationStatus.SUCCESS ||
                status == IntegrationStatus.BUSINESS_FAILURE) {
            tx.setCompletedAt(LocalDateTime.now());
        }

        repository.save(tx);
    }

    private IntegrationTransaction get(Long transactionId) {
        return repository.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException(
                        "Integration transaction not found: " + transactionId
                ));
    }
}
