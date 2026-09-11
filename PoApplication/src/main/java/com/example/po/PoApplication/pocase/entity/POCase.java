package com.example.po.PoApplication.pocase.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(
    name = "po_case",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_po_case_case_id",
            columnNames = "case_id"
        )
    }
)
public class POCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false, unique = true)
    private String caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "debit_account", nullable = false)
    private String debitAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;

    @Column(name = "pay_order_required", nullable = false)
    private Boolean payOrderRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private POStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "po_number", unique = true)
    private String poNumber;

    @Column(name = "flex_request_id")
    private String flexRequestId;
}
