package com.example.po.integration.repository;

import com.example.po.integration.entity.IntegrationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationTransactionRepository
        extends JpaRepository<IntegrationTransaction, Long> {

    Optional<IntegrationTransaction>  findByIdempotencyKey(String idempotencyKey);

    Optional<IntegrationTransaction> findByRequestId(String requestId);

    Optional<IntegrationTransaction> findByCaseIdAndOperation(String caseId,String operation);
}