package com.example.po.integration.service;

import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.entity.OutboxStatus;
import com.example.po.integration.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxResultService {

    private final OutboxEventRepository repository;

    public OutboxResultService(
            OutboxEventRepository repository) {

        this.repository = repository;
    }

    @Transactional
    public void markCompleted(Long eventId) {

        OutboxEvent event =
                repository.findById(eventId)
                        .orElseThrow();

        event.setStatus(
                OutboxStatus.COMPLETED
        );

        event.setUpdatedAt(
                LocalDateTime.now()
        );

        event.setLockedAt(null);

        repository.save(event);
    }


    @Transactional
    public void handleFailure(
            OutboxEvent event,
            Exception exception) {

        OutboxEvent current =
                repository.findById(
                        event.getId()
                ).orElseThrow();

        current.setLastError(
                exception.getMessage()
        );

        current.setLockedAt(null);
        current.setUpdatedAt(
                LocalDateTime.now()
        );

        if (current.getAttempts() >= 5) {

            current.setStatus(
                    OutboxStatus.RECONCILIATION_REQUIRED
            );

        } else {

            current.setStatus(
                    OutboxStatus.RETRY
            );

            current.setAvailableAt(
                    calculateNextAttempt(
                            current.getAttempts()
                    )
            );
        }

        repository.save(current);
    }


    //calculate next attempt
    private LocalDateTime calculateNextAttempt(
            int attempt) {

        long seconds =
                Math.min(
                        300,
                        5L * (long) Math.pow(3, attempt - 1)
                );

        return LocalDateTime.now()
                .plusSeconds(seconds);
    }
}