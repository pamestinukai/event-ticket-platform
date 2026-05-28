package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;
import com.pamestinukai.backend.dtos.response.TicketResponseDTO;
import com.pamestinukai.backend.dtos.response.TicketValidationResponseDTO;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.repositories.TicketRepository;
import com.pamestinukai.backend.services.interfaces.ITicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final ITicketService ticketService;
    private final TicketRepository ticketRepository;
    private final PurchaseRepository purchaseRepository;

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
    public ResponseEntity<Void> confirmTicketReservation(
            @PathVariable Long purchaseId,
            @RequestParam(required = false) String buyerEmail,
            @RequestParam(required = false) String buyerName
    ){
        ticketService.confirmTicketReservation(purchaseId, buyerEmail, buyerName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/purchase/{purchaseId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByPurchase(@PathVariable Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        List<TicketResponseDTO> tickets = ticketRepository.findAllByPurchase(purchase)
                .stream()
                .map(t -> {
                    TicketResponseDTO dto = new TicketResponseDTO();
                    dto.setTicketId(t.getTicketId());
                    dto.setQrToken(t.getQrToken());
                    dto.setTicketTypeName(t.getTicketType().getName());
                    dto.setStatus(t.getStatus().name());
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/validate/{qrToken}")
    public ResponseEntity<TicketValidationResponseDTO> validateTicket(@PathVariable String qrToken) {
        return ResponseEntity.ok(ticketService.validateTicketToken(qrToken));
    }

    @PostMapping("/check-in/{qrToken}")
    public ResponseEntity<TicketValidationResponseDTO> checkInTicket(@PathVariable String qrToken) {
        return ResponseEntity.ok(ticketService.checkInTicket(qrToken));
    }
}
