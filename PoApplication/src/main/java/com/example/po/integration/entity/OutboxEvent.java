package com.example.po.integration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "outbox_event",
        indexes = {
                @Index(
                        name = "idx_outbox_status_available",
                        columnList = "status, available_at"
                ),
                @Index(
                        name = "idx_outbox_aggregate",
                        columnList = "aggregate_id"
                )
        }
)
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true
    )
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false
    )
    private OutboxEventType eventType;

    @Column(
            name = "aggregate_type",
            nullable = false
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false
    )
    private String aggregateId;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(
            name = "available_at",
            nullable = false
    )
    private LocalDateTime availableAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(
            name = "last_error",
            columnDefinition = "TEXT"
    )
    private String lastError;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

}

