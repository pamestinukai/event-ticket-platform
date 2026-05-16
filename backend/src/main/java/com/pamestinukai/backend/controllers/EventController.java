package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.dtos.request.EventFilterRequestDTO;
import com.pamestinukai.backend.dtos.response.EventResponseDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.services.interfaces.IEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final IEventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @ModelAttribute EventFilterRequestDTO filter) {
        return ResponseEntity.ok(
                eventService.getEvents(filter).map(this::mapToDTO)
        );
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents() {
        List<Event> events = eventService.getAvailableEvents();
        List<EventResponseDTO> eventResponseDTOS = events.stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(eventResponseDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        Event event = eventService.getEvent(id);
        return ResponseEntity.ok(mapToDTO(event));
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO eventRequestDTO) {
        Event event = eventService.createEvent(eventRequestDTO);
        return ResponseEntity.status(201).body(mapToDTO(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequestDTO eventRequestDTO) {
        Event event = eventService.updateEvent(id, eventRequestDTO);
        return ResponseEntity.ok(mapToDTO(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EventResponseDTO> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    private EventResponseDTO mapToDTO(Event event){
        EventResponseDTO dto = new EventResponseDTO();
        dto.setEventId(event.getEventId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setPerformers(event.getPerformers());
        dto.setImages(event.getImages());
        dto.setStartDatetime(event.getStartDatetime());
        dto.setEndDatetime(event.getEndDatetime());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setOrganizationName(event.getOrganization().getCompanyName());
        dto.setVenueName(event.getVenue().getName());
        dto.setAuditoriumName(event.getAuditorium().getName());
        dto.setCategoryName(event.getCategory().getName());
        return dto;
    }
}
