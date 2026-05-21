package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.response.TicketTypeResponseDTO;
import com.pamestinukai.backend.mappers.TicketTypeResponseMapper;
import com.pamestinukai.backend.services.interfaces.ITicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events/{eventId}/ticket-types")
public class TicketTypeController {

    private final ITicketTypeService ticketTypeService;
    private final TicketTypeResponseMapper ticketTypeResponseMapper;

    @GetMapping
    public ResponseEntity<List<TicketTypeResponseDTO>> getByEvent(@PathVariable Long eventId) {
        List<TicketTypeResponseDTO> ticketTypes = ticketTypeService.getByEventId(eventId).stream()
                .map(ticketTypeResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(ticketTypes);
    }
}
