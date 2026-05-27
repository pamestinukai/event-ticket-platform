package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.dtos.request.EventFilterRequestDTO;
import com.pamestinukai.backend.dtos.response.EventAnalyticsResponseDTO;
import com.pamestinukai.backend.entities.Event;

import org.springframework.data.domain.Page;
import java.util.List;

public interface IEventService {

    Page<Event> getEvents(EventFilterRequestDTO filter);
    List<Event> getAvailableEvents();
    Event getEvent(Long id);
    Event getAvailableEvent(Long id);
    EventAnalyticsResponseDTO getEventAnalytics(Long id, Long organizationId);
    Event createEvent(EventRequestDTO eventRequestDTO);
    Event updateEvent(Long id, EventRequestDTO eventRequestDTO);
    void deleteEvent(Long id);
    void assertPurchasable(Event event);
}
