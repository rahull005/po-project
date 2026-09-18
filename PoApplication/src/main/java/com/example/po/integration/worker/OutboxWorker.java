package com.example.po.integration.worker;

import com.example.po.integration.service.OutboxProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxWorker {
    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxProcessor processor;

    public OutboxWorker(OutboxProcessor outBoxProcessor){
        this.processor = outBoxProcessor;
    }

    @Scheduled(fixedDelayString = "${outbox.worker.fixed-delay-ms:2000}")
    private void process(){
        try {

            processor.processNext();

        } catch (Exception e) {

            log.error(
                    "Unexpected error in outbox worker",
                    e
            );
        }
    }
}
