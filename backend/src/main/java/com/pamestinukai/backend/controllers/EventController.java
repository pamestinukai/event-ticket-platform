package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.dtos.request.EventFilterRequestDTO;
import com.pamestinukai.backend.dtos.response.EventResponseDTO;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.mappers.EventResponseMapper;
import com.pamestinukai.backend.services.interfaces.IEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final IEventService eventService;
    private final EventResponseMapper eventResponseMapper;

    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @ModelAttribute EventFilterRequestDTO filter) {
        return ResponseEntity.ok(
                eventService.getEvents(filter).map(eventResponseMapper::toDTO)
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<EventResponseDTO>> getMyEvents(
            @AuthenticationPrincipal Employee employee,
            @ModelAttribute EventFilterRequestDTO filter) {
        if (employee == null || employee.getOrganization() == null) {
            return ResponseEntity.ok(Page.empty());
        }
        filter.setOrganizationId(employee.getOrganization().getOrganizationId());
        return ResponseEntity.ok(
                eventService.getEvents(filter).map(eventResponseMapper::toDTO)
        );
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<EventResponseDTO>> searchPublicEvents(
            @ModelAttribute EventFilterRequestDTO filter) {
        if (filter.getStatus() == null) {
            filter.setStatus(Event.EventStatus.PUBLISHED);
        }
        return ResponseEntity.ok(
                eventService.getEvents(filter).map(eventResponseMapper::toDTO)
        );
    }

    @GetMapping("/public/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents() {
        List<Event> events = eventService.getAvailableEvents();
        List<EventResponseDTO> eventResponseDTOS = events.stream()
                .map(eventResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(eventResponseDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        Event event = eventService.getEvent(id);
        return ResponseEntity.ok(eventResponseMapper.toDTO(event));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<EventResponseDTO> getAvailableEventById(@PathVariable Long id) {
        Event event = eventService.getAvailableEvent(id);
        return ResponseEntity.ok(eventResponseMapper.toDTO(event));
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @AuthenticationPrincipal Employee employee,
            @Valid @RequestBody EventRequestDTO eventRequestDTO) {
        if (employee != null && employee.getOrganization() != null) {
            eventRequestDTO.setOrganizationId(employee.getOrganization().getOrganizationId());
        }
        Event event = eventService.createEvent(eventRequestDTO);
        return ResponseEntity.status(201).body(eventResponseMapper.toDTO(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee,
            @Valid @RequestBody EventRequestDTO eventRequestDTO) {
        if (employee != null && employee.getOrganization() != null) {
            eventRequestDTO.setOrganizationId(employee.getOrganization().getOrganizationId());
        }
        Event event = eventService.updateEvent(id, eventRequestDTO);
        return ResponseEntity.ok(eventResponseMapper.toDTO(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EventResponseDTO> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
