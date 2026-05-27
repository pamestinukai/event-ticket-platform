package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;
import com.pamestinukai.backend.services.interfaces.ITicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final ITicketService ticketService;

    @PostMapping("/reserve")
    public ResponseEntity<TicketReservationResponseDTO> reserveTickets(@RequestBody @Valid TicketReservationRequestDTO dto){
        TicketReservationResponseDTO responseDTO = ticketService.reserveTicket(dto);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/reserve/{purchaseId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long purchaseId){
        ticketService.cancelTicketReservation(purchaseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserve/{purchaseId}/confirm")
    public ResponseEntity<Void> confirmTicketReservation(@PathVariable Long purchaseId){
        ticketService.confirmTicketReservation(purchaseId);
        return ResponseEntity.noContent().build();
    }
}
