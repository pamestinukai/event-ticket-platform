package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.TicketReservationItemDTO;
import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;
import com.pamestinukai.backend.dtos.response.TicketValidationResponseDTO;
import com.pamestinukai.backend.entities.CheckIn;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Notification;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.DuplicateTicketTokenException;
import com.pamestinukai.backend.exceptions.InvalidTicketStatusException;
import com.pamestinukai.backend.exceptions.InvalidTicketTokenException;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.mappers.TicketMapper;
import com.pamestinukai.backend.repositories.CheckInRepository;
import com.pamestinukai.backend.repositories.EventRepository;
import com.pamestinukai.backend.repositories.NotificationRepository;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.repositories.TicketRepository;
import com.pamestinukai.backend.repositories.TicketTypeRepository;
import com.pamestinukai.backend.services.email.EmailAttachment;
import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import com.pamestinukai.backend.services.interfaces.ITicketService;
import com.pamestinukai.backend.services.interfaces.ITicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
   private final NotificationRepository notificationRepository;
   private final CheckInRepository checkInRepository;
   private final TicketMapper ticketMapper;
   private final IEmailService emailService;
   private final TicketQrCodeService ticketQrCodeService;
   private final TicketPdfService ticketPdfService;

   @Value("${app.mail.retry.delay-minutes:5}")
   private long emailRetryDelayMinutes;

   private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
      confirmTicketReservation(purchaseId, null, null);
   }

   @Override
   public void confirmTicketReservation(Long purchaseId, String buyerEmail, String buyerName){
      Purchase purchase = purchaseRepository.findById(purchaseId)
              .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

      if (buyerEmail != null && !buyerEmail.isBlank()) {
         purchase.setBuyerEmail(buyerEmail);
      }
      if (buyerName != null && !buyerName.isBlank()) {
         purchase.setBuyerName(buyerName);
      }

      if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING)
         throw new InvalidTicketStatusException("Purchase has already been made or cancelled");

      List<Ticket> tickets = ticketRepository.findAllByPurchase(purchase);

      for (Ticket ticket : tickets){
         if (ticket.getStatus() != Ticket.TicketStatus.RESERVED)
            throw new InvalidTicketStatusException("The ticket is already valid or canceled");
         ticket.setStatus(Ticket.TicketStatus.VALID);
         if (ticket.getQrToken() == null || ticket.getQrToken().isBlank()) {
            ticket.setQrToken(generateUniqueQrToken());
         }
      }

      ticketRepository.saveAll(tickets);

      purchase.setStatus(Purchase.PurchaseStatus.COMPLETED);
      purchaseRepository.save(purchase);

            Ticket representativeTicket = tickets.stream().findFirst()
               .orElseThrow(() -> new InvalidTicketStatusException("No tickets found for purchase"));

            Notification notification = createConfirmationNotification(representativeTicket);
            notificationRepository.save(notification);
            sendPurchaseEmailWithRetryState(purchase, tickets, notification);

      log.info("Tickets confirmed for purchase id: {}", purchaseId);
   }

   @Override
   public TicketValidationResponseDTO validateTicketToken(String qrToken) {
      Ticket ticket = resolveTicketByToken(qrToken);
      if (ticket.getStatus() == Ticket.TicketStatus.CANCELED || ticket.getStatus() == Ticket.TicketStatus.REFUNDED) {
         throw new InvalidTicketStatusException("Ticket is no longer active");
      }
      return toValidationResponse(ticket);
   }

   @Override
   public TicketValidationResponseDTO checkInTicket(String qrToken) {
      Ticket ticket = resolveTicketByToken(qrToken);

      if (ticket.getStatus() == Ticket.TicketStatus.CHECKED_IN || checkInRepository.existsByTicket(ticket)) {
         throw new InvalidTicketStatusException("Ticket has already been checked in");
      }

      if (ticket.getStatus() != Ticket.TicketStatus.VALID) {
         throw new InvalidTicketStatusException("Ticket is not valid for check-in");
      }

      CheckIn checkIn = new CheckIn();
      checkIn.setTicket(ticket);
      checkIn.setScannedAt(LocalDateTime.now());
      checkIn.setScanResult(CheckIn.ScanResult.SUCCESS);
      checkInRepository.save(checkIn);

      ticket.setStatus(Ticket.TicketStatus.CHECKED_IN);
      ticketRepository.save(ticket);
      return toValidationResponse(ticket);
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
            ticket.setQrToken(generateUniqueQrToken());
            ticket.setIssuedAt(LocalDateTime.now());
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

   private Ticket resolveTicketByToken(String qrToken) {
      long duplicates = ticketRepository.countByQrToken(qrToken);
      if (duplicates > 1) {
         throw new DuplicateTicketTokenException("Duplicate ticket token detected");
      }

      return ticketRepository.findFirstByQrToken(qrToken)
              .orElseThrow(() -> new InvalidTicketTokenException("Ticket token is invalid"));
   }

   private Notification createConfirmationNotification(Ticket ticket) {
      Notification notification = new Notification();
      notification.setPurchase(ticket.getPurchase());
      notification.setEvent(ticket.getTicketType().getEvent());
      notification.setTicket(ticket);
      notification.setType(Notification.NotificationType.CONFIRMATION);
      notification.setStatus(Notification.NotificationStatus.SCHEDULED);
      notification.setScheduledAt(LocalDateTime.now());
      notification.setAttemptCount(0);
      return notification;
   }

   private void sendPurchaseEmailWithRetryState(Purchase purchase, List<Ticket> tickets, Notification notification) {
      try {
         String recipient = purchase.getBuyerEmail();
         if (recipient == null || recipient.isBlank()) {
            throw new IllegalStateException("Buyer email is missing for purchase " + purchase.getPurchaseId());
         }

         Ticket firstTicket = tickets.stream().findFirst()
                 .orElseThrow(() -> new IllegalStateException("No tickets found for purchase " + purchase.getPurchaseId()));

         EmailMessage.EmailMessageBuilder messageBuilder = EmailMessage.builder()
                 .to(recipient)
                 .subject("Your tickets for " + firstTicket.getTicketType().getEvent().getTitle())
                 .templateName("ticket")
                 .variable("recipientName", defaultValue(purchase.getBuyerName(), "there"))
                 .variable("eventTitle", defaultValue(firstTicket.getTicketType().getEvent().getTitle(), "Event"))
                 .variable("eventDate", formatEventDate(firstTicket.getTicketType().getEvent()))
                 .variable("venue", formatVenue(firstTicket.getTicketType().getEvent()))
                 .variable("ticketType", tickets.size() > 1 ? "Multiple ticket types" : defaultValue(firstTicket.getTicketType().getName(), "General"))
                 .variable("ticketId", tickets.size() > 1 ? "Included in attached PDFs" : firstTicket.getQrToken());

         for (Ticket ticket : tickets) {
            byte[] qrCodePng = ticketQrCodeService.generatePng(ticket.getQrToken());
            byte[] ticketPdf = ticketPdfService.generateTicketPdf(ticket, qrCodePng);
            messageBuilder.attachment(EmailAttachment.pdf("ticket-" + ticket.getTicketId() + ".pdf", ticketPdf));
         }

         emailService.send(messageBuilder.build());

         notification.setStatus(Notification.NotificationStatus.SENT);
         notification.setSentAt(LocalDateTime.now());
         notification.setLastError(null);
      } catch (Exception ex) {
         notification.setStatus(Notification.NotificationStatus.FAILED);
         notification.setAttemptCount(notification.getAttemptCount() == null ? 1 : notification.getAttemptCount() + 1);
         notification.setLastError(truncateError(ex.getMessage()));
         notification.setScheduledAt(LocalDateTime.now().plusMinutes(emailRetryDelayMinutes));
         log.error("Failed to send ticket email for purchase {}", purchase.getPurchaseId(), ex);
      }

      notificationRepository.save(notification);
   }

   private TicketValidationResponseDTO toValidationResponse(Ticket ticket) {
      TicketValidationResponseDTO response = new TicketValidationResponseDTO();
      response.setTicketId(ticket.getTicketId());
      response.setEventTitle(defaultValue(ticket.getTicketType().getEvent().getTitle(), "Unknown event"));
      response.setEventDate(formatEventDate(ticket.getTicketType().getEvent()));
      response.setVenue(formatVenue(ticket.getTicketType().getEvent()));
      response.setBuyerName(defaultValue(ticket.getPurchase().getBuyerName(), "Guest"));
      response.setTicketType(defaultValue(ticket.getTicketType().getName(), "General"));
      response.setStatus(ticket.getStatus().name());
      response.setToken(ticket.getQrToken());
      return response;
   }

   private String generateUniqueQrToken() {
      String token = UUID.randomUUID().toString();
      while (ticketRepository.countByQrToken(token) > 0) {
         token = UUID.randomUUID().toString();
      }
      return token;
   }

   private static String defaultValue(String value, String fallback) {
      return value == null || value.isBlank() ? fallback : value;
   }

   private static String formatEventDate(Event event) {
      if (event.getStartDatetime() == null) {
         return "TBA";
      }
      return event.getStartDatetime().format(EVENT_DATE_FORMAT);
   }

   private static String formatVenue(Event event) {
      if (event.getVenue() == null) {
         return "TBA";
      }

      String venueName = defaultValue(event.getVenue().getName(), "Unknown venue");
      String city = defaultValue(event.getVenue().getCity(), "");
      String address = defaultValue(event.getVenue().getAddress(), "");
      return (venueName + " " + city + " " + address).trim();
   }

   private static String truncateError(String errorMessage) {
      if (errorMessage == null || errorMessage.isBlank()) {
         return "Email delivery failed";
      }
      return errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage;
   }
}
