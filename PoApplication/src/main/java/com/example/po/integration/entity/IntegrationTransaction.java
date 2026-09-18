package com.example.po.integration.entity;

import com.example.po.pocase.entity.IntegrationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "integration_transaction",
        indexes = {
                @Index(
                        name = "idx_integration_case_id",
                        columnList = "case_id"
                ),
                @Index(
                        name = "idx_integration_request_id",
                        columnList = "request_id"
                )
        }
)
public class IntegrationTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private String caseId;

    @Column(name = "system_name", nullable = false)
    private String systemName;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

}