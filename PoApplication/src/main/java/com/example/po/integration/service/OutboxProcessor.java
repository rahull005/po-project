package com.example.po.integration.service;

import com.example.po.integration.entity.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OutboxProcessor.class
            );

    private final OutboxClaimService claimService;
    private final POApprovedEventHandler eventHandler;
    private final OutboxResultService resultService;

    public OutboxProcessor(
            OutboxClaimService claimService,
            POApprovedEventHandler eventHandler,
            OutboxResultService resultService) {

        this.claimService = claimService;
        this.eventHandler = eventHandler;
        this.resultService = resultService;
    }

    public void processNext() {

        OutboxEvent event =
                claimService.claimNext();

        if (event == null) {
            return;
        }

        log.info(
                "Processing outbox event eventId={} type={} aggregateId={} attempt={}",
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAttempts()
        );

        try {

            switch (event.getEventType()) {

                case PO_APPROVED ->
                        eventHandler.handle(
                                event
                        );

                default ->
                        throw new IllegalStateException(
                                "Unsupported outbox event type: "
                                        + event.getEventType()
                        );
            }

            resultService.markCompleted(
                    event.getId()
            );

            log.info(
                    "Outbox event completed eventId={} aggregateId={}",
                    event.getEventId(),
                    event.getAggregateId()
            );

        } catch (Exception e) {

            log.error(
                    "Outbox event processing failed eventId={} aggregateId={} attempt={}",
                    event.getEventId(),
                    event.getAggregateId(),
                    event.getAttempts(),
                    e
            );

            resultService.handleFailure(
                    event,
                    e
            );
        }
    }


    //recover events from stuck for long
    public void recoverStaleEvents() {
        claimService.recoverStaleEvents(5);
    }
}
