package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne
    @JoinColumn(name = "auditorium_id")
    private Auditorium auditorium;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String title;
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> performers;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> images;

    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EventStatus {
        DRAFT, PUBLISHED, CANCELED, RESCHEDULED, COMPLETED
    }
}
