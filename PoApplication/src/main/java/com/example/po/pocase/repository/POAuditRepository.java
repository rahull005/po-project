package com.example.po.pocase.repository;

import com.example.po.pocase.entity.POAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface POAuditRepository
        extends JpaRepository<POAudit, Long> {

    List<POAudit> findByPoCaseCaseIdOrderByCreatedAtAsc(
            String caseId
    );
}