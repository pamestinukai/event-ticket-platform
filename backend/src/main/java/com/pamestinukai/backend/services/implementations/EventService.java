package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.repositories.*;
import com.pamestinukai.backend.services.interfaces.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    private final OrganizationRepository organizationRepository;
    private final VenueRepository venueRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Event> getEvents(){
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event createEvent(EventRequestDTO eventRequestDTO){
        Event event = mapToEntity(new Event(), eventRequestDTO);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, EventRequestDTO eventRequestDTO){
        Event event = getEvent(id);
        mapToEntity(event, eventRequestDTO);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id){
        eventRepository.deleteById(id);
    }

    private Event mapToEntity(Event event, EventRequestDTO dto) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setPerformers(dto.getPerformers());
        event.setImages(dto.getImages());
        event.setStartDatetime(dto.getStartDatetime());
        event.setEndDatetime(dto.getEndDatetime());
        event.setStatus(dto.getStatus());

        if (dto.getOrganizationId() != null)
            event.setOrganization(organizationRepository.findById(dto.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Organization not found")));

        if (dto.getVenueId() != null)
            event.setVenue(venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found")));

        if (dto.getAuditoriumId() != null)
            event.setAuditorium(auditoriumRepository.findById(dto.getAuditoriumId())
                    .orElseThrow(() -> new RuntimeException("Auditorium not found")));

        if (dto.getCategoryId() != null)
            event.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));

        return event;
    }
}
