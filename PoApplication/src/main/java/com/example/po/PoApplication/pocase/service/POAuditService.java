package com.example.po.PoApplication.pocase.service;


import com.example.po.PoApplication.pocase.entity.AuditAction;
import com.example.po.PoApplication.pocase.entity.POAudit;
import com.example.po.PoApplication.pocase.entity.POCase;
import com.example.po.PoApplication.pocase.entity.POStatus;
import com.example.po.PoApplication.pocase.repository.POAuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class POAuditService {

    private final POAuditRepository auditRepository;

    public POAuditService(POAuditRepository auditRepository){
        this.auditRepository = auditRepository;
    }

    public void record(POCase poCase, AuditAction action, POStatus oldStatus,POStatus newStatus,String performedBy,String remarks){
        POAudit poAudit = new POAudit();
        poAudit.setPoCase(poCase);
        poAudit.setAction(action);
        poAudit.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        poAudit.setNewStatus(newStatus != null ? newStatus.name() : null);
        poAudit.setPerformedBy(performedBy);
        poAudit.setRemarks(remarks);
        poAudit.setCreatedAt(LocalDateTime.now());

        auditRepository.save(poAudit);
    }
}
