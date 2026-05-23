package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.TicketTypeRequestDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.EventStatusException;
import com.pamestinukai.backend.exceptions.InsufficientTicketsException;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.EventRepository;
import com.pamestinukai.backend.repositories.TicketTypeRepository;
import com.pamestinukai.backend.services.interfaces.ITicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketTypeService implements ITicketTypeService {

    private static final Set<Event.EventStatus> OPEN_STATUSES = Set.of(
            Event.EventStatus.PUBLISHED,
            Event.EventStatus.RESCHEDULED
    );

    private static final Set<Event.EventStatus> BLOCKED_STATUSES = Set.of(
            Event.EventStatus.CANCELED,
            Event.EventStatus.SOLD_OUT,
            Event.EventStatus.COMPLETED
    );

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketType> getByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return ticketTypeRepository.findByEvent(event);
    }

    @Override
    public void syncForEvent(Event event, List<TicketTypeRequestDTO> requested) {
        if (requested == null) return;

        List<TicketType> existing = ticketTypeRepository.findByEvent(event);

        existing.stream()
                .filter(e -> requested.stream().noneMatch(r -> r.getName().equals(e.getName())))
                .forEach(ticketTypeRepository::delete);

        for (TicketTypeRequestDTO dto : requested) {
            existing.stream()
                    .filter(e -> e.getName().equals(dto.getName()))
                    .findFirst()
                    .ifPresentOrElse(
                            existingType -> {
                                int sold = existingType.getTotalQuantity() - existingType.getAvailableQuantity();
                                if (dto.getTotalQuantity() < sold) {
                                    throw new IllegalArgumentException(
                                            "New total quantity (%d) for '%s' cannot be less than already-sold tickets (%d)"
                                                    .formatted(dto.getTotalQuantity(), dto.getName(), sold));
                                }
                                existingType.setPrice(dto.getPrice());
                                existingType.setTotalQuantity(dto.getTotalQuantity());
                                existingType.setAvailableQuantity(dto.getTotalQuantity() - sold);
                                ticketTypeRepository.save(existingType);
                            },

                            () -> {
                                TicketType type = new TicketType();
                                type.setEvent(event);
                                type.setName(dto.getName());
                                type.setPrice(dto.getPrice());
                                type.setTotalQuantity(dto.getTotalQuantity());
                                type.setAvailableQuantity(dto.getTotalQuantity());
                                ticketTypeRepository.save(type);
                            }
                    );
        }

        reevaluateEventStatus(event);
    }

    @Override
    public void reserveTickets(Long ticketTypeId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");

        TicketType ticketType = findTicketType(ticketTypeId);
        Event event = ticketType.getEvent();

        if (BLOCKED_STATUSES.contains(event.getStatus())) {
            throw new EventStatusException(
                    "Tickets cannot be purchased for an event with status: " + event.getStatus());
        }

        if (ticketType.getAvailableQuantity() < quantity) {
            throw new InsufficientTicketsException(
                    "Only %d ticket(s) available for type '%s'"
                            .formatted(ticketType.getAvailableQuantity(), ticketType.getName()));
        }

        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() - quantity);
        ticketTypeRepository.save(ticketType);

        log.debug("Reserved {} ticket(s) for TicketType {} (event {})", quantity, ticketTypeId, event.getEventId());

        reevaluateEventStatus(event);
    }

    @Override
    public void releaseTickets(Long ticketTypeId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");

        TicketType ticketType = findTicketType(ticketTypeId);
        int newAvailable = ticketType.getAvailableQuantity() + quantity;

        if (newAvailable > ticketType.getTotalQuantity()) {
            throw new IllegalArgumentException(
                    "Cannot release %d ticket(s): would exceed total quantity of %d"
                            .formatted(quantity, ticketType.getTotalQuantity()));
        }

        ticketType.setAvailableQuantity(newAvailable);
        ticketTypeRepository.save(ticketType);

        log.debug("Released {} ticket(s) for TicketType {} (event {})", quantity, ticketTypeId,
                ticketType.getEvent().getEventId());

        reevaluateEventStatus(ticketType.getEvent());
    }

    void reevaluateEventStatus(Event event) {
        if (event.getStatus() == Event.EventStatus.CANCELED) return;

        Event.EventStatus derived = deriveStatus(event);
        if (derived != event.getStatus()) {
            log.info("Event {} status: {} → {}", event.getEventId(), event.getStatus(), derived);
            event.setStatus(derived);
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
        }
    }

    private Event.EventStatus deriveStatus(Event event) {
        if (LocalDateTime.now().isAfter(event.getEndDatetime())) {
            return Event.EventStatus.COMPLETED;
        }

        boolean isOpenOrSoldOut = OPEN_STATUSES.contains(event.getStatus())
                || event.getStatus() == Event.EventStatus.SOLD_OUT;

        if (!isOpenOrSoldOut) return event.getStatus();

        List<TicketType> types = ticketTypeRepository.findByEvent(event);

        if (types.isEmpty()) return event.getStatus();

        boolean allExhausted = types.stream().allMatch(tt -> tt.getAvailableQuantity() == 0);

        if (allExhausted) return Event.EventStatus.SOLD_OUT;

        if (event.getStatus() == Event.EventStatus.SOLD_OUT) {
            return Event.EventStatus.PUBLISHED;
        }

        return event.getStatus();
    }

    private TicketType findTicketType(Long ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + ticketTypeId));
    }
}