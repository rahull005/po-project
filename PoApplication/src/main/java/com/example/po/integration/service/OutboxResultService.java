package com.example.po.integration.service;

import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.entity.OutboxStatus;
import com.example.po.integration.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxResultService {

    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository repository;

    public OutboxResultService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void markCompleted(Long eventId) {
        OutboxEvent event = get(eventId);
        event.setStatus(OutboxStatus.COMPLETED);
        event.setLockedAt(null);
        event.setUpdatedAt(LocalDateTime.now());
        repository.save(event);
    }

    @Transactional
    public void markBusinessFailure(
            Long eventId,
            Exception exception) {
        OutboxEvent event = get(eventId);
        event.setStatus(OutboxStatus.COMPLETED);
        event.setLastError(exception.getMessage());
        event.setLockedAt(null);
        event.setUpdatedAt(LocalDateTime.now());
        repository.save(event);
    }

    @Transactional
    public void markReconciliationRequired(
            Long eventId,
            Exception exception) {
        OutboxEvent event = get(eventId);
        event.setStatus(OutboxStatus.RECONCILIATION_REQUIRED);
        event.setLastError(exception.getMessage());
        event.setLockedAt(null);
        event.setUpdatedAt(LocalDateTime.now());
        repository.save(event);
    }

    @Transactional
    public void handleRetryableFailure(
            OutboxEvent event,
            Exception exception) {

        OutboxEvent current = get(event.getId());
        current.setLastError(exception.getMessage());
        current.setLockedAt(null);
        current.setUpdatedAt(LocalDateTime.now());

        if (current.getAttempts() >= MAX_ATTEMPTS) {
            current.setStatus(OutboxStatus.RECONCILIATION_REQUIRED);
        } else {
            current.setStatus(OutboxStatus.RETRY);
            current.setAvailableAt(
                    calculateNextAttempt(current.getAttempts())
            );
        }

        repository.save(current);
    }

    private LocalDateTime calculateNextAttempt(int attempt) {
        long seconds = Math.min(
                300,
                5L * (long) Math.pow(3, Math.max(0, attempt - 1))
        );
        return LocalDateTime.now().plusSeconds(seconds);
    }

    private OutboxEvent get(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                ));
    }
}
