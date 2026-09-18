package com.example.po.integration.service;

import com.example.po.integration.dto.POApprovedEvent;
import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.entity.OutboxEventType;
import com.example.po.integration.entity.OutboxStatus;
import com.example.po.integration.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper) {

        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void createPOApprovedEvent(
            String caseId,
            Long approvalId) {

        try {

            POApprovedEvent payload =
                    new POApprovedEvent(
                            caseId,
                            approvalId
                    );

            OutboxEvent event =
                    new OutboxEvent();

            LocalDateTime now =
                    LocalDateTime.now();

            event.setEventId(
                    UUID.randomUUID()
            );

            event.setEventType(
                    OutboxEventType.PO_APPROVED
            );

            event.setAggregateType(
                    "PO_CASE"
            );

            event.setAggregateId(
                    caseId
            );

            event.setPayload(
                    objectMapper.writeValueAsString(
                            payload
                    )
            );

            event.setStatus(
                    OutboxStatus.NEW
            );

            event.setAttempts(0);

            event.setAvailableAt(now);
            event.setCreatedAt(now);
            event.setUpdatedAt(now);

            repository.save(event);

        } catch (JacksonException e) {

            throw new IllegalStateException(
                    "Failed to serialize PO approved event",
                    e
            );
        }
    }
}
