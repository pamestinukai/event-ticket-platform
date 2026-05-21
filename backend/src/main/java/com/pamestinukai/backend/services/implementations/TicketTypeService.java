package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.TicketTypeRequestDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.EventRepository;
import com.pamestinukai.backend.repositories.TicketTypeRepository;
import com.pamestinukai.backend.services.interfaces.ITicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketTypeService implements ITicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<TicketType> getByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return ticketTypeRepository.findByEvent(event);
    }

    public void syncForEvent(Event event, List<TicketTypeRequestDTO> requested) {
        if (requested == null) return;
        ticketTypeRepository.deleteByEvent(event);
        for (TicketTypeRequestDTO dto : requested) {
            TicketType type = new TicketType();
            type.setEvent(event);
            type.setName(dto.getName());
            type.setPrice(dto.getPrice());
            type.setTotalQuantity(dto.getTotalQuantity());
            type.setAvailableQuantity(dto.getTotalQuantity());
            ticketTypeRepository.save(type);
        }
    }
}
