package com.example.po.PoApplication.pocase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "po_approval")
@Getter
@Setter
public class POApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "po_case_id",
            nullable = false
    )
    private POCase poCase;

    @Column(nullable = false)
    private String makerId;

    private String checkerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime actionAt;

}