package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    private Long entityId;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    private String changes;
    private LocalDateTime createdAt;

    public enum EntityType { EVENT, TICKET_TYPE, SEAT_LAYOUT, AUDITORIUM }
    public enum AuditAction { CREATE, UPDATE, DELETE }
}
