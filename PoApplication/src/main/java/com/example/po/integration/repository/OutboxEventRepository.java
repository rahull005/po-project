package com.example.po.integration.repository;

import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    Optional<OutboxEvent> findByEventId(UUID eventId);

    Optional<OutboxEvent> findFirstByAggregateIdAndEventTypeAndStatus(
            String aggregateId,
            String eventType,
            OutboxStatus status
    );
}
