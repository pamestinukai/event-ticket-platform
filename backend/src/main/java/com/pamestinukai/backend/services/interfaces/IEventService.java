package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.EventRequestDTO;
import com.pamestinukai.backend.entities.Event;

import java.util.List;

public interface IEventService {

    List<Event> getEvents();
    List<Event> getAvailableEvents();
    Event getEvent(Long id);
    Event getAvailableEvent(Long id);
    Event createEvent(EventRequestDTO eventRequestDTO);
    Event updateEvent(Long id, EventRequestDTO eventRequestDTO);
    void deleteEvent(Long id);
}
