package com.example.po.integration.service;

import com.example.po.integration.dto.POApprovedEvent;
import com.example.po.integration.entity.OutboxEvent;
import com.example.po.pocase.entity.POCase;
import com.example.po.pocase.entity.POStatus;
import com.example.po.pocase.repository.POCaseRepository;
import com.example.po.pocase.service.POProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class POApprovedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    POApprovedEventHandler.class
            );

    private final ObjectMapper objectMapper;
    private final POCaseRepository poCaseRepository;
    private final POProcessingService processingService;

    public POApprovedEventHandler(
            ObjectMapper objectMapper,
            POCaseRepository poCaseRepository,
            POProcessingService processingService) {

        this.objectMapper = objectMapper;
        this.poCaseRepository = poCaseRepository;
        this.processingService = processingService;
    }

    public void handle(
            OutboxEvent outboxEvent)
            throws Exception {

        POApprovedEvent event =
                objectMapper.readValue(
                        outboxEvent.getPayload(),
                        POApprovedEvent.class
                );

        POCase poCase =
                poCaseRepository
                        .findByCaseId(
                                event.caseId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "PO case not found: "
                                                + event.caseId()
                                ));

        log.info(
                "Handling PO_APPROVED event caseId={} eventId={}",
                event.caseId(),
                outboxEvent.getEventId()
        );

        if (poCase.getStatus() != POStatus.APPROVED) {

            log.warn(
                    "PO case not in APPROVED state caseId={} actualStatus={}",
                    poCase.getCaseId(),
                    poCase.getStatus()
            );

            return;
        }

        processingService.processWithFlex(
                poCase.getCaseId()
        );
    }
}
