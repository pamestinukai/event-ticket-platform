package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.TicketReservationItemDTO;
import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.InvalidTicketStatusException;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.mappers.TicketMapper;
import com.pamestinukai.backend.repositories.EventRepository;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.repositories.TicketRepository;
import com.pamestinukai.backend.repositories.TicketTypeRepository;
import com.pamestinukai.backend.services.interfaces.ITicketService;
import com.pamestinukai.backend.services.interfaces.ITicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class TicketService implements ITicketService {

   private final TicketRepository ticketRepository;
   private final ITicketTypeService ticketTypeService;
   private final TicketTypeRepository ticketTypeRepository;
   private final EventRepository eventRepository;
   private final PurchaseRepository purchaseRepository;
   private final TicketMapper ticketMapper;

   public TicketReservationResponseDTO reserveTicket(TicketReservationRequestDTO dto){

      Event event = eventRepository.findById(dto.getEventId())
              .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

      Map<Long, TicketType> ticketTypeMap = fetchAndValidateTicketTypes(dto);
      Purchase purchase = createPendingPurchase();
      List<Ticket> tickets = createReservedTickets(dto.getTickets(), ticketTypeMap, purchase);
      ticketRepository.saveAll(tickets);
      BigDecimal totalPrice = calculatePrice(dto.getTickets(), ticketTypeMap);

      log.info("Tickets reserved");
      return ticketMapper.toReservationResponseDTO(dto, event, ticketTypeMap, purchase, totalPrice);
   }

   public void cancelTicketReservation(Long purchaseId){
      Purchase purchase = purchaseRepository.findById(purchaseId)
              .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

      if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING)
         throw new InvalidTicketStatusException("Purchase has already been made or cancelled");
      List<Ticket> tickets = ticketRepository.findAllByPurchase(purchase);

      // release tickets and set ticket status to canceled
      for(Ticket ticket : tickets){
         if (ticket.getStatus() != Ticket.TicketStatus.RESERVED)
            throw new InvalidTicketStatusException("The ticket is already valid or canceled");
         ticketTypeService.releaseTickets(ticket.getTicketType().getTicketTypeId(), 1);
         ticket.setStatus(Ticket.TicketStatus.CANCELED);
      }

      ticketRepository.saveAll(tickets);

      // set purchase status to failed
      purchase.setStatus(Purchase.PurchaseStatus.FAILED);
      purchaseRepository.save(purchase);

      log.info("Tickets canceled for purchase id: {}", purchaseId);
   }

   public void confirmTicketReservation(Long purchaseId){
      Purchase purchase = purchaseRepository.findById(purchaseId)
              .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

      if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING)
         throw new InvalidTicketStatusException("Purchase has already been made or cancelled");

      List<Ticket> tickets = ticketRepository.findAllByPurchase(purchase);

      for (Ticket ticket : tickets){
         if (ticket.getStatus() != Ticket.TicketStatus.RESERVED)
            throw new InvalidTicketStatusException("The ticket is already valid or canceled");
         ticket.setStatus(Ticket.TicketStatus.VALID);
      }
      // call method to generate qrToken (TO DO)

      ticketRepository.saveAll(tickets);
      log.info("Tickets confirmed for purchase id: {}", purchaseId);
   }

   private Map<Long, TicketType> fetchAndValidateTicketTypes(TicketReservationRequestDTO dto) {
      List<Long> ids = dto.getTickets().stream()
              .map(TicketReservationItemDTO::getTicketTypeId)
              .toList();

      Map<Long, TicketType> map = ticketTypeRepository.findAllById(ids).stream()
              .collect(Collectors.toMap(TicketType::getTicketTypeId, tt -> tt));

      for (TicketType tt : map.values()) {
         if (!tt.getEvent().getEventId().equals(dto.getEventId())) {
            throw new IllegalArgumentException("Ticket type " + tt.getTicketTypeId() + " does not belong to event " + dto.getEventId());
         }
      }

      return map;
   }

   private Purchase createPendingPurchase() {
      Purchase purchase = new Purchase();
      purchase.setStatus(Purchase.PurchaseStatus.PENDING);
      purchase.setCreatedAt(LocalDateTime.now());
      purchase.setCurrency("EUR");
      return purchaseRepository.save(purchase);
   }

   private List<Ticket> createReservedTickets(
           List<TicketReservationItemDTO> items,
           Map<Long, TicketType> ticketTypeMap,
           Purchase purchase
   ) {
      List<Ticket> tickets = new ArrayList<>();
      for (TicketReservationItemDTO item : items) {
         ticketTypeService.reserveTickets(item.getTicketTypeId(), item.getQuantity());
         TicketType ticketType = ticketTypeMap.get(item.getTicketTypeId());
         if (ticketType == null) throw new ResourceNotFoundException("Ticket type not found");
         for (int i = 0; i < item.getQuantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setTicketType(ticketType);
            ticket.setStatus(Ticket.TicketStatus.RESERVED);
            ticket.setPurchase(purchase);
            ticket.setIssuedAt(LocalDateTime.now());
            ticket.setQrToken(java.util.UUID.randomUUID().toString());
            tickets.add(ticket);
         }
      }
      return tickets;
   }

   private BigDecimal calculatePrice(List<TicketReservationItemDTO> items, Map<Long, TicketType> ticketTypeMap) {

      BigDecimal totalPrice = BigDecimal.ZERO;
      for (TicketReservationItemDTO item : items) {
         TicketType tt = ticketTypeMap.get(item.getTicketTypeId());
         if (tt == null) throw new ResourceNotFoundException("Ticket type not found");
         totalPrice = totalPrice.add(tt.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
      }
      return totalPrice;
   }
}
