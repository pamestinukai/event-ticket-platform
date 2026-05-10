package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
