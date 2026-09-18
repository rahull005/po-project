package com.example.po.integration.service;

import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.entity.OutboxStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxClaimService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public OutboxEvent claimNext() {

        List<Long> ids =
                entityManager
                        .createNativeQuery("""
                            SELECT id
                            FROM outbox_event
                            WHERE status IN ('NEW', 'RETRY')
                              AND available_at <= CURRENT_TIMESTAMP
                            ORDER BY created_at
                            FOR UPDATE SKIP LOCKED
                            LIMIT 1
                        """)
                        .getResultList();

        if (ids.isEmpty()) {
            return null;
        }

        Long id = ids.get(0);

        OutboxEvent event =
                entityManager.find(
                        OutboxEvent.class,
                        id
                );

        event.setStatus(
                OutboxStatus.PROCESSING
        );

        event.setAttempts(
                event.getAttempts() + 1
        );

        event.setLockedAt(
                LocalDateTime.now()
        );

        event.setUpdatedAt(
                LocalDateTime.now()
        );

        entityManager.flush();

        return event;
    }
}