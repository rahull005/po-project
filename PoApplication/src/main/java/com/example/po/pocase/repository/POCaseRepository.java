package com.example.po.pocase.repository;

import com.example.po.pocase.entity.POCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface POCaseRepository extends JpaRepository<POCase,Long> {
    Optional<POCase> findByCaseId(String caseId);

    boolean existsByCaseId(String caseId);
}
