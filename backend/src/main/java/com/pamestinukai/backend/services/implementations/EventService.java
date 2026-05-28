package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.EventFilterRequestDTO;
import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.dtos.response.EventAnalyticsResponseDTO;
import com.pamestinukai.backend.dtos.response.EventTicketTypeAnalyticsDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.EventStatusException;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.*;
import com.pamestinukai.backend.services.interfaces.IEventNotificationService;
import com.pamestinukai.backend.services.interfaces.IEventService;
import com.pamestinukai.backend.services.interfaces.ITicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService implements IEventService {

    private static final List<Event.EventStatus> PURCHASABLE_STATUSES = List.of(
            Event.EventStatus.PUBLISHED,
            Event.EventStatus.RESCHEDULED
    );

    private static final Set<Event.EventStatus> BLOCKED_FOR_PURCHASE = Set.of(
            Event.EventStatus.CANCELED,
            Event.EventStatus.SOLD_OUT,
            Event.EventStatus.COMPLETED
    );

        private static final List<Ticket.TicketStatus> SOLD_STATUSES = List.of(
            Ticket.TicketStatus.VALID,
            Ticket.TicketStatus.CHECKED_IN
        );

    private final EventRepository eventRepository;
    private final OrganizationRepository organizationRepository;
    private final VenueRepository venueRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final ITicketTypeService ticketTypeService;
    private final IEventNotificationService eventNotificationService;

    @Transactional(readOnly = true)
    public List<Event> getAvailableEvents() {
        return eventRepository.findByStatusIn(PURCHASABLE_STATUSES);
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @Transactional(readOnly = true)
    public Event getAvailableEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        assertPurchasable(event);
        return event;
    }

    @Transactional(readOnly = true)
    public EventAnalyticsResponseDTO getEventAnalytics(Long id, Long organizationId) {
        Event event = getEvent(id);
        if (organizationId == null
                || event.getOrganization() == null
                || !organizationId.equals(event.getOrganization().getOrganizationId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view analytics for your organization's events"
            );
        }

        if (event.getEndDatetime() == null || event.getEndDatetime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Analytics are available only for past events");
        }

        int totalCapacity = ticketTypeRepository.sumTotalQuantityByEventId(id);
        int ticketsSold = (int) ticketRepository.countByEventIdAndStatuses(id, SOLD_STATUSES);
        int checkedIn = (int) ticketRepository.countByEventIdAndStatuses(
                id,
                List.of(Ticket.TicketStatus.CHECKED_IN)
        );
        BigDecimal revenue = ticketRepository.sumRevenueByEventIdAndStatuses(
                id,
                SOLD_STATUSES
        );

        List<EventTicketTypeAnalyticsDTO> byType = ticketTypeRepository.findByEvent(event)
                .stream()
            .map(this::mapTicketTypeAnalytics)
                .toList();

        EventAnalyticsResponseDTO analytics = new EventAnalyticsResponseDTO();
        analytics.setEventId(event.getEventId());
        analytics.setEventTitle(event.getTitle());
        analytics.setTotalCapacity(totalCapacity);
        analytics.setTicketsSold(ticketsSold);
        analytics.setCheckedIn(checkedIn);
        analytics.setRevenue(revenue);
        analytics.setAttendanceRate(ticketsSold == 0 ? 0.0 : (checkedIn * 100.0) / ticketsSold);
        analytics.setTicketTypeAnalytics(byType);
        return analytics;
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

    public Event createEvent(EventRequestDTO dto) {
        validateEventTime(dto);
        Event event = mapToEntity(new Event(), dto);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        Event saved = eventRepository.save(event);
        ticketTypeService.syncForEvent(saved, dto.getTicketTypes());
        return saved;
    }

    public Event updateEvent(Long id, EventRequestDTO dto) {
        validateEventTime(dto);
        Event event = getEvent(id);
        Event.EventStatus oldStatus = event.getStatus();
        LocalDateTime oldStartDatetime = event.getStartDatetime();

        mapToEntity(event, dto);
        event.setUpdatedAt(LocalDateTime.now());
        Event saved = eventRepository.save(event);
        ticketTypeService.syncForEvent(saved, dto.getTicketTypes());

        notifyOnLifecycleChange(saved, oldStatus, oldStartDatetime);
        return saved;
    }

    private void notifyOnLifecycleChange(Event saved, Event.EventStatus oldStatus, LocalDateTime oldStart) {
        boolean canceled = oldStatus != Event.EventStatus.CANCELED
                && saved.getStatus() == Event.EventStatus.CANCELED;
        if (canceled) {
            eventNotificationService.notifyCancellation(saved);
            return;
        }

        boolean targetIsLive = saved.getStatus() != Event.EventStatus.DRAFT
                && saved.getStatus() != Event.EventStatus.CANCELED
                && saved.getStatus() != Event.EventStatus.COMPLETED;
        boolean dateChanged = !Objects.equals(oldStart, saved.getStartDatetime());
        boolean statusChangedToRescheduled = oldStatus != Event.EventStatus.RESCHEDULED
                && saved.getStatus() == Event.EventStatus.RESCHEDULED;

        if (targetIsLive && (dateChanged || statusChangedToRescheduled)) {
            eventNotificationService.notifyReschedule(saved);
        }
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found");
        }
        eventRepository.deleteById(id);
    }

    public void assertPurchasable(Event event) {
        if (BLOCKED_FOR_PURCHASE.contains(event.getStatus())) {
            String reason = switch (event.getStatus()) {
                case CANCELED  -> "This event has been cancelled.";
                case SOLD_OUT  -> "This event is sold out.";
                case COMPLETED -> "This event has already taken place.";
                default        -> "Tickets are not available for this event.";
            };
            throw new EventStatusException(reason);
        }
    }

        private EventTicketTypeAnalyticsDTO mapTicketTypeAnalytics(TicketType ticketType) {
            long soldForType = ticketRepository.countByTicketTypeIdAndStatuses(
                ticketType.getTicketTypeId(),
                SOLD_STATUSES
            );
            long checkedInForType = ticketRepository.countByTicketTypeIdAndStatuses(
                ticketType.getTicketTypeId(),
                List.of(Ticket.TicketStatus.CHECKED_IN)
            );
            BigDecimal revenueForType = ticketRepository.sumRevenueByTicketTypeIdAndStatuses(
                ticketType.getTicketTypeId(),
                SOLD_STATUSES
            );

        EventTicketTypeAnalyticsDTO dto = new EventTicketTypeAnalyticsDTO();
        dto.setTicketTypeId(ticketType.getTicketTypeId());
        dto.setTicketTypeName(ticketType.getName());
            dto.setTicketsSold((int) soldForType);
            dto.setCheckedIn((int) checkedInForType);
            dto.setRevenue(revenueForType);
        return dto;
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
                categoryId != null ? cb.equal(root.get("category").get("categoryId"), categoryId) : null;
    }

    private Specification<Event> hasVenue(Long venueId) {
        return (root, query, cb) ->
                venueId != null ? cb.equal(root.get("venue").get("venueId"), venueId) : null;
    }

    private Specification<Event> hasOrganization(Long organizationId) {
        return (root, query, cb) ->
                organizationId != null ? cb.equal(root.get("organization").get("organizationId"), organizationId) : null;
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
                status != null ? cb.equal(root.get("status"), status) : null;
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

    private void validateEventTime(EventRequestDTO dto) {
        if (dto.getStartDatetime().isAfter(dto.getEndDatetime())) {
            throw new IllegalArgumentException("Start datetime must be before end datetime");
        }
    }
}
