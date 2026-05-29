package com.pamestinukai.backend.dtos.request;

import com.pamestinukai.backend.entities.Event;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EventRequestDTO {

    @NotNull(message = "Organization is required")
    private Long organizationId;

    @NotNull(message = "Venue is required")
    private Long venueId;

    private Long auditoriumId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be up to 100 characters long")
    private String title;

    private String description;

    private List<String> performers;
    private List<String> images;

    @NotNull(message = "Start datetime is required")
    @Future(message = "Start datetime must be in the future")
    private LocalDateTime startDatetime;

    @NotNull(message = "End datetime is required")
    @Future(message = "End datetime must be in the future")
    private LocalDateTime endDatetime;

    @NotNull(message = "Status is required")
    private Event.EventStatus status;

    @Valid
    @NotEmpty(message = "At least one ticket type is required")
    private List<TicketTypeRequestDTO> ticketTypes;
}