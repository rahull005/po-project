package com.example.po.integration.service;

import com.example.po.flex.infrastructure.exception.FlexBusinessException;
import com.example.po.flex.infrastructure.exception.FlexSystemException;
import com.example.po.flex.infrastructure.exception.FlexTimeoutException;
import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxEventRepository repository;
    private final POApprovedEventHandler eventHandler;
    private final OutboxResultService resultService;
    private final OutboxClaimService claimService;

    public OutboxProcessor(
            OutboxEventRepository repository,
            POApprovedEventHandler eventHandler,
            OutboxResultService resultService,
            OutboxClaimService claimService) {
        this.repository = repository;
        this.eventHandler = eventHandler;
        this.resultService = resultService;
        this.claimService = claimService;
    }

    public void process(Long eventId) {

        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                ));

        String previousCorrelationId = MDC.get("correlationId");
        if (previousCorrelationId == null) {
            MDC.put("correlationId", "OUTBOX-" + event.getEventId());
        }

        log.info(
                "Starting async outbox processing eventId={} type={} aggregateId={} attempt={}",
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAttempts()
        );

        try {
            switch (event.getEventType()) {
                case PO_APPROVED -> eventHandler.handle(event);
                default -> throw new IllegalStateException(
                        "Unsupported outbox event type: " + event.getEventType()
                );
            }

            resultService.markCompleted(event.getId());

            log.info(
                    "Outbox event completed eventId={} aggregateId={}",
                    event.getEventId(),
                    event.getAggregateId()
            );

        } catch (FlexTimeoutException e) {
            log.warn(
                    "Flex timeout requires reconciliation eventId={} aggregateId={}",
                    event.getEventId(),
                    event.getAggregateId()
            );
            resultService.markReconciliationRequired(event.getId(), e);

        } catch (FlexBusinessException e) {
            log.warn(
                    "Flex business failure eventId={} aggregateId={} errorCode={}",
                    event.getEventId(),
                    event.getAggregateId(),
                    e.getErrorCode()
            );
            resultService.markBusinessFailure(event.getId(), e);

        } catch (FlexSystemException e) {
            log.error(
                    "Flex system failure eventId={} aggregateId={} attempt={}",
                    event.getEventId(),
                    event.getAggregateId(),
                    event.getAttempts(),
                    e
            );
            resultService.handleRetryableFailure(event, e);

        } catch (Exception e) {
            log.error(
                    "Outbox processing failed eventId={} aggregateId={} attempt={}",
                    event.getEventId(),
                    event.getAggregateId(),
                    event.getAttempts(),
                    e
            );
            resultService.handleRetryableFailure(event, e);
        } finally {
            if (previousCorrelationId == null) {
                MDC.remove("correlationId");
            }
        }
    }

    public void recoverStaleEvents(int staleLockMinutes) {
        claimService.recoverStaleEvents(staleLockMinutes);
    }
}
