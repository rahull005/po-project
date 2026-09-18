package com.example.po.pocase.repository;

import com.example.po.pocase.entity.ApprovalStatus;
import com.example.po.pocase.entity.POApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface POApprovalRepository
        extends JpaRepository<POApproval, Long> {

    List<POApproval> findByPoCaseCaseId(String caseId);

    Optional<POApproval> findFirstByPoCaseCaseIdAndStatusOrderByCreatedAtDesc(
            String caseId,
            ApprovalStatus status
    );
}
