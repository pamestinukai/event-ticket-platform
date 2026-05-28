package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.ReservedTicketSummaryDTO;
import com.pamestinukai.backend.dtos.request.CreateCheckoutSessionRequestDTO;
import com.pamestinukai.backend.dtos.response.CreateCheckoutSessionResponseDTO;
import com.pamestinukai.backend.dtos.response.PurchaseSummaryResponseDTO;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.Venue;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.repositories.TicketRepository;
import com.pamestinukai.backend.services.interfaces.IStripeService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final IStripeService stripeService;
    private final PurchaseRepository purchaseRepository;
    private final TicketRepository ticketRepository;

    @PostMapping("/checkout-session")
    public ResponseEntity<CreateCheckoutSessionResponseDTO> createCheckoutSession(
            @RequestBody @Valid CreateCheckoutSessionRequestDTO dto) {
        try {
            CreateCheckoutSessionResponseDTO response = stripeService.createCheckoutSession(dto);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error creating checkout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripeService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error handling Stripe webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/purchase/{purchaseId}/summary")
    public ResponseEntity<PurchaseSummaryResponseDTO> getPurchaseSummary(@PathVariable Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        List<Ticket> tickets = ticketRepository.findAllByPurchase(purchase);
        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("No tickets found for purchase");
        }

        var event = tickets.get(0).getTicketType().getEvent();
        Venue venue = event.getVenue();

        // Group tickets by type for summary
        Map<Long, ReservedTicketSummaryDTO> summaryMap = new LinkedHashMap<>();
        for (Ticket ticket : tickets) {
            Long typeId = ticket.getTicketType().getTicketTypeId();
            summaryMap.compute(typeId, (id, existing) -> {
                if (existing == null) {
                    ReservedTicketSummaryDTO dto = new ReservedTicketSummaryDTO();
                    dto.setTicketTypeId(typeId);
                    dto.setTicketTypeName(ticket.getTicketType().getName());
                    dto.setQuantity(1);
                    dto.setPricePerTicket(ticket.getTicketType().getPrice());
                    return dto;
                }
                existing.setQuantity(existing.getQuantity() + 1);
                return existing;
            });
        }

        BigDecimal total = summaryMap.values().stream()
                .map(s -> s.getPricePerTicket().multiply(BigDecimal.valueOf(s.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseSummaryResponseDTO response = new PurchaseSummaryResponseDTO();
        response.setPurchaseId(purchase.getPurchaseId());
        response.setBuyerName(purchase.getBuyerName());
        response.setBuyerEmail(purchase.getBuyerEmail());
        response.setTotalPrice(total);
        response.setCurrency(purchase.getCurrency());
        response.setStatus(purchase.getStatus().name());
        response.setEventId(event.getEventId());
        response.setEventName(event.getTitle());
        response.setEventStartDatetime(event.getStartDatetime() != null ? event.getStartDatetime().toString() : null);
        response.setEventEndDatetime(event.getEndDatetime() != null ? event.getEndDatetime().toString() : null);
        response.setVenueName(venue != null ? venue.getName() : null);
        response.setVenueCity(venue != null ? venue.getCity() : null);
        response.setAuditoriumName(event.getAuditorium() != null ? event.getAuditorium().getName() : null);
        response.setEventImages(event.getImages());
        response.setTickets(new ArrayList<>(summaryMap.values()));

        return ResponseEntity.ok(response);
    }
}
