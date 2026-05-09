package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationId;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private Employee owner;

    @Column(unique = true, nullable = false)
    private String companyName;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
