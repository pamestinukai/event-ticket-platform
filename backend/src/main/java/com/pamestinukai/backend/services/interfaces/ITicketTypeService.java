package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.TicketTypeRequestDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.TicketType;

import java.util.List;

public interface ITicketTypeService {
    List<TicketType> getByEventId(Long eventId);
    void syncForEvent(Event event, List<TicketTypeRequestDTO> requested);
}
