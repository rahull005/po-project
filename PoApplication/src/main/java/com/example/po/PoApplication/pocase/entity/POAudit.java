package com.example.po.PoApplication.pocase.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "po_audit",
        indexes = {
                @Index(name = "idx_po_audit_case_id", columnList = "po_case_id"),
                @Index(name = "idx_po_audit_created_at", columnList = "created_at")
        }
)
public class POAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "po_case_id", nullable = false)
    private POCase poCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public POAudit() {
    }

    public POAudit(Long id, POCase poCase, AuditAction action, String oldStatus, String newStatus, String performedBy, String remarks, LocalDateTime createdAt) {
        this.id = id;
        this.poCase = poCase;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.performedBy = performedBy;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public POCase getPoCase() {
        return poCase;
    }

    public void setPoCase(POCase poCase) {
        this.poCase = poCase;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
