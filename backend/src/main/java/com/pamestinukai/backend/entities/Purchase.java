package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseId;

    private String buyerName;
    private String buyerEmail;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentProvider;
    private String providerTransactionId;
    private String stripeSessionId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private PurchaseStatus status;

    private LocalDateTime createdAt;

    public enum PurchaseStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}
