package com.example.po.integration.worker;

import com.example.po.integration.service.OutboxProcessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OutboxAsyncExecutor {

    private final OutboxProcessor processor;

    public OutboxAsyncExecutor(OutboxProcessor processor) {
        this.processor = processor;
    }

    @Async("outboxTaskExecutor")
    public void processAsync(Long eventId) {
        processor.process(eventId);
    }
}
