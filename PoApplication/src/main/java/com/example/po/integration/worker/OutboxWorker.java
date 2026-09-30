package com.example.po.integration.worker;

import com.example.po.integration.entity.OutboxEvent;
import com.example.po.integration.service.OutboxClaimService;
import com.example.po.integration.service.OutboxProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxWorker {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxClaimService claimService;
    private final OutboxProcessor processor;
    private final OutboxAsyncExecutor asyncExecutor;
    private final int batchSize;
    private final int staleLockMinutes;

    public OutboxWorker(
            OutboxClaimService claimService,
            OutboxProcessor processor,
            OutboxAsyncExecutor asyncExecutor,
            @Value("${outbox.worker.batch-size:10}") int batchSize,
            @Value("${outbox.worker.stale-lock-minutes:5}") int staleLockMinutes) {
        this.claimService = claimService;
        this.processor = processor;
        this.asyncExecutor = asyncExecutor;
        this.batchSize = batchSize;
        this.staleLockMinutes = staleLockMinutes;
    }

    @Scheduled(
            fixedDelayString = "${outbox.worker.fixed-delay-ms:2000}"
    )
    public void dispatch() {

        processor.recoverStaleEvents(staleLockMinutes);

        for (int i = 0; i < batchSize; i++) {
            OutboxEvent event = claimService.claimNext();

            if (event == null) {
                return;
            }

            log.info(
                    "Dispatching outbox event asynchronously eventId={} aggregateId={} attempt={}",
                    event.getEventId(),
                    event.getAggregateId(),
                    event.getAttempts()
            );

            asyncExecutor.processAsync(event.getId());
        }
    }
}
