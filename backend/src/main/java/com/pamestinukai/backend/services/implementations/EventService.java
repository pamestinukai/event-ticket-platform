package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.EventFilterRequestDTO;
import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.*;
import com.pamestinukai.backend.services.interfaces.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public List<Event> getAvailableEvents(){
        List<Event.EventStatus> availableStatuses = List.of(
                Event.EventStatus.PUBLISHED,
                Event.EventStatus.RESCHEDULED
        );
        return eventRepository.findByStatusIn(availableStatuses);
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @Transactional(readOnly = true)
    public Page<Event> getEvents(EventFilterRequestDTO filter) {
        if (StringUtils.hasText(filter.getPerformer())) {
            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
            return eventRepository.findByPerformer(filter.getPerformer(), pageable);
        }
        Specification<Event> spec = buildSpec(filter);
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDir());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        return eventRepository.findAll(spec, pageable);
    }

    public Event createEvent(EventRequestDTO eventRequestDTO){
        validateEventTime(eventRequestDTO);
        Event event = mapToEntity(new Event(), eventRequestDTO);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, EventRequestDTO eventRequestDTO){
        validateEventTime(eventRequestDTO);
        Event event = getEvent(id);
        mapToEntity(event, eventRequestDTO);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id){
        if (!eventRepository.existsById(id)){
            throw new ResourceNotFoundException("Event not found");
        }
        eventRepository.deleteById(id);
    }

    private Specification<Event> buildSpec(EventFilterRequestDTO f) {
        return Specification
                .where(hasKeyword(f.getKeyword()))
                .and(hasCategory(f.getCategoryId()))
                .and(hasVenue(f.getVenueId()))
                .and(hasOrganization(f.getOrganizationId()))
                .and(hasDateBetween(f.getDateFrom(), f.getDateTo()))
                .and(hasStatus(f.getStatus()));
    }

    private Specification<Event> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private Specification<Event> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId != null
                        ? cb.equal(root.get("category").get("categoryId"), categoryId)
                        : null;
    }

    private Specification<Event> hasVenue(Long venueId) {
        return (root, query, cb) ->
                venueId != null
                        ? cb.equal(root.get("venue").get("venueId"), venueId)
                        : null;
    }

    private Specification<Event> hasOrganization(Long organizationId) {
        return (root, query, cb) ->
                organizationId != null
                        ? cb.equal(root.get("organization").get("organizationId"), organizationId)
                        : null;
    }

    private Specification<Event> hasDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(root.get("startDatetime"), to);
            if (to == null)   return cb.greaterThanOrEqualTo(root.get("startDatetime"), from);
            return cb.between(root.get("startDatetime"), from, to);
        };
    }

    private Specification<Event> hasStatus(Event.EventStatus status) {
        return (root, query, cb) ->
                status != null
                        ? cb.equal(root.get("status"), status)
                        : null;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field = switch (sortBy != null ? sortBy : "") {
            case "title" -> "title";
            default      -> "startDatetime";
        };
        return "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
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
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found")));

        if (dto.getVenueId() != null)
            event.setVenue(venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found")));

        if (dto.getAuditoriumId() != null)
            event.setAuditorium(auditoriumRepository.findById(dto.getAuditoriumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Auditorium not found")));

        if (dto.getCategoryId() != null)
            event.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found")));

        return event;
    }

    private void validateEventTime(EventRequestDTO dto){
        if(dto.getStartDatetime().isAfter(dto.getEndDatetime())){
            throw new IllegalArgumentException("Start datetime must be before end datetime");
        }
    }
}