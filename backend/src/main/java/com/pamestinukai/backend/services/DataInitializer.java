package com.pamestinukai.backend.services;

import com.pamestinukai.backend.entities.*;
import com.pamestinukai.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final VenueRepository venueRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final CategoryRepository categoryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final PurchaseRepository purchaseRepository;
    private final CheckInRepository checkInRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        boolean manualInit = List.of(args).contains("--init-data");

        if (!manualInit) {
            return;
        }

        // seed the full demo dataset only once
        if (eventRepository.count() == 0) {

        // --- Categories ---
        Category music = new Category();
        music.setName("Music");
        music.setDescription("Live music concerts and festivals");
        categoryRepository.save(music);

        Category sports = new Category();
        sports.setName("Sports");
        sports.setDescription("Sporting events and competitions");
        categoryRepository.save(sports);

        Category theater = new Category();
        theater.setName("Theater");
        theater.setDescription("Plays, musicals and performances");
        categoryRepository.save(theater);

        // --- org Owners---
        Employee owner1 = new Employee();
        owner1.setEmail("owner1@livenation.com");
        owner1.setPasswordHash(passwordEncoder.encode("password1"));
        owner1.setPhone("+37061111111");
        owner1.setActive(true);
        owner1.setCreatedAt(LocalDateTime.now());
        owner1.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(owner1);

        Employee owner2 = new Employee();
        owner2.setEmail("owner2@eventpro.com");
        owner2.setPasswordHash(passwordEncoder.encode("password2"));
        owner2.setPhone("+37062222222");
        owner2.setActive(true);
        owner2.setCreatedAt(LocalDateTime.now());
        owner2.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(owner2);

        // --- Organizations ---
        Organization org1 = new Organization();
        org1.setOwner(owner1);
        org1.setCompanyName("Live Nation Lithuania");
        org1.setActive(true);
        org1.setCreatedAt(LocalDateTime.now());
        org1.setUpdatedAt(LocalDateTime.now());
        organizationRepository.save(org1);

        Organization org2 = new Organization();
        org2.setOwner(owner2);
        org2.setCompanyName("EventPro");
        org2.setActive(true);
        org2.setCreatedAt(LocalDateTime.now());
        org2.setUpdatedAt(LocalDateTime.now());
        organizationRepository.save(org2);

        // --- Link employees to their organizations ---
        owner1.setOrganization(org1);
        employeeRepository.save(owner1);

        owner2.setOrganization(org2);
        employeeRepository.save(owner2);

        // --- Venues ---
        Venue venue1 = new Venue();
        venue1.setName("Siemens Arena");
        venue1.setAddress("Ozo g. 14");
        venue1.setCity("Vilnius");
        venue1.setCountry("Lithuania");
        venueRepository.save(venue1);

        Venue venue2 = new Venue();
        venue2.setName("Žalgiris Arena");
        venue2.setAddress("Karaliaus Mindaugo pr. 50");
        venue2.setCity("Kaunas");
        venue2.setCountry("Lithuania");
        venueRepository.save(venue2);

        // --- Auditoriums ---
        Auditorium aud1 = new Auditorium();
        aud1.setVenue(venue1);
        aud1.setName("Main Hall");
        aud1.setTotalCapacity(15000);
        auditoriumRepository.save(aud1);

        Auditorium aud2 = new Auditorium();
        aud2.setVenue(venue1);
        aud2.setName("Small Hall");
        aud2.setTotalCapacity(3000);
        auditoriumRepository.save(aud2);

        Auditorium aud3 = new Auditorium();
        aud3.setVenue(venue2);
        aud3.setName("Main Arena");
        aud3.setTotalCapacity(15200);
        auditoriumRepository.save(aud3);

        // --- Events ---
        Event event1 = new Event();
        event1.setTitle("Rock Night 2026");
        event1.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam at bibendum felis." +
                " Integer sapien libero, congue nec pretium vitae, fermentum a ex." +
                " In id mattis ipsum. Mauris posuere lectus magna. Cras id erat et felis dapibus tempor. " +
                "Praesent auctor quam sed venenatis ultrices. Aenean vel finibus mauris. " +
                "Phasellus pellentesque imperdiet diam nec blandit. Suspendisse in imperdiet justo, vitae consectetur lacus. Nunc\n" +
                "\n" +
                "luctus tortor mi, vel viverra tortor efficitur eu. Nulla consequat vulputate pharetra. Proin at risus fermentum," +
                " finibus nibh sit amet, consectetur orci. Vestibulum malesuada felis ex, sagittis commodo elit sodales quis. " +
                "Proin tempus tristique mi, aliquet tempus erat rutrum vitae. Morbi lectus ipsum, tincidunt nec efficitur eu, " +
                "vulputate sit amet ipsum. Sed scelerisque pharetra nisl. Nunc molestie purus ut turpis fringilla, " +
                "pharetra aliquet nisl iaculis. Sed ut ligula id lorem convallis dictum. Vestibulum ultrices, elit ac pulvinar malesuada," +
                " nisl velit porta eros, sed pellentesque nibh felis non diam. Mauris malesuada suscipit est sit amet dictum. Quisque quis " +
                "elementum dui. Donec varius erat in lacus feugiat posuere. Praesent quis rutrum nisi. Interdum et malesuada fames ac ante" +
                " ipsum primis in faucibus. Suspendisse mi turpis, porttitor et ullamcorper quis, iaculis eget quam. " +
                "Maecenas placerat accumsan orci, sed ultricies dolor sodales facilisis. Aenean nec orci eu dui bibendum sodales ut ac neque." +
                " Etiam vel dui euismod, viverra enim sit amet, feugiat diam. Sed sollicitudin metus ac leo efficitur auctor." +
                " Fusce at neque sapien. Suspendisse et aliquam tortor, ut faucibus felis. In hac habitasse platea dictumst." +
                " Proin finibus mollis velit at hendrerit. Maecenas vitae turpis augue. Etiam eget metus ipsum." +
                " Quisque scelerisque faucibus venenatis. Nullam sed vestibulum dui. Pellentesque ultricies dolor a justo dignissim accumsan. ");
        event1.setOrganization(org1);
        event1.setVenue(venue1);
        event1.setAuditorium(aud1);
        event1.setCategory(music);
        event1.setPerformers(List.of("Rammstein", "Metallica"));
        event1.setImages(List.of("rock_night_1.jpg", "rock_night_2.jpg"));
        event1.setStatus(Event.EventStatus.PUBLISHED);
        event1.setStartDatetime(LocalDateTime.now().plusDays(30));
        event1.setEndDatetime(LocalDateTime.now().plusDays(30).plusHours(4));
        event1.setCreatedAt(LocalDateTime.now());
        event1.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setTitle("Basketball Championship");
        event2.setDescription("National basketball league finals.");
        event2.setOrganization(org2);
        event2.setVenue(venue2);
        event2.setAuditorium(aud3);
        event2.setCategory(sports);
        event2.setPerformers(List.of("Žalgiris", "Rytas"));
        event2.setImages(List.of("basketball_1.jpg"));
        event2.setStatus(Event.EventStatus.PUBLISHED);
        event2.setStartDatetime(LocalDateTime.now().plusDays(15));
        event2.setEndDatetime(LocalDateTime.now().plusDays(15).plusHours(2));
        event2.setCreatedAt(LocalDateTime.now());
        event2.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event2);

        Event event3 = new Event();
        event3.setTitle("Hamlet");
        event3.setDescription("Shakespeare's classic tragedy performed live.");
        event3.setOrganization(org1);
        event3.setVenue(venue1);
        event3.setAuditorium(aud2);
        event3.setCategory(theater);
        event3.setPerformers(List.of("National Drama Theatre"));
        event3.setImages(List.of("hamlet_1.jpg", "hamlet_2.jpg"));
        event3.setStatus(Event.EventStatus.DRAFT);
        event3.setStartDatetime(LocalDateTime.now().plusDays(60));
        event3.setEndDatetime(LocalDateTime.now().plusDays(60).plusHours(3));
        event3.setCreatedAt(LocalDateTime.now());
        event3.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event3);

        // --- Ticket Types for Rock Night 2026 ---
        TicketType rockVip = new TicketType();
        rockVip.setEvent(event1);
        rockVip.setName("VIP");
        rockVip.setPrice(new BigDecimal("149.99"));
        rockVip.setTotalQuantity(200);
        rockVip.setAvailableQuantity(200);
        ticketTypeRepository.save(rockVip);

        TicketType rockStanding = new TicketType();
        rockStanding.setEvent(event1);
        rockStanding.setName("Standing");
        rockStanding.setPrice(new BigDecimal("49.99"));
        rockStanding.setTotalQuantity(5000);
        rockStanding.setAvailableQuantity(5000);
        ticketTypeRepository.save(rockStanding);

        TicketType rockSeated = new TicketType();
        rockSeated.setEvent(event1);
        rockSeated.setName("Seated");
        rockSeated.setPrice(new BigDecimal("79.99"));
        rockSeated.setTotalQuantity(3000);
        rockSeated.setAvailableQuantity(3000);
        ticketTypeRepository.save(rockSeated);

        // --- Ticket Types for Basketball Championship ---
        TicketType basketballVip = new TicketType();
        basketballVip.setEvent(event2);
        basketballVip.setName("VIP");
        basketballVip.setPrice(new BigDecimal("99.99"));
        basketballVip.setTotalQuantity(100);
        basketballVip.setAvailableQuantity(100);
        ticketTypeRepository.save(basketballVip);

        TicketType basketballGeneral = new TicketType();
        basketballGeneral.setEvent(event2);
        basketballGeneral.setName("General Admission");
        basketballGeneral.setPrice(new BigDecimal("29.99"));
        basketballGeneral.setTotalQuantity(8000);
        basketballGeneral.setAvailableQuantity(8000);
        ticketTypeRepository.save(basketballGeneral);

        // --- Ticket Types for Hamlet ---
        TicketType hamletPremium = new TicketType();
        hamletPremium.setEvent(event3);
        hamletPremium.setName("Premium");
        hamletPremium.setPrice(new BigDecimal("59.99"));
        hamletPremium.setTotalQuantity(500);
        hamletPremium.setAvailableQuantity(500);
        ticketTypeRepository.save(hamletPremium);

        TicketType hamletStandard = new TicketType();
        hamletStandard.setEvent(event3);
        hamletStandard.setName("Standard");
        hamletStandard.setPrice(new BigDecimal("29.99"));
        hamletStandard.setTotalQuantity(2000);
        hamletStandard.setAvailableQuantity(2000);
        ticketTypeRepository.save(hamletStandard);

            seedPastAnalyticsEvent(org1, venue1, aud1, music);
            System.out.println("Data initialized successfully");
            return;
        }

        // allow already-initialized environments to get analytics demo data as well
        Organization organization = organizationRepository.findAll().stream().findFirst().orElse(null);
        Venue venue = venueRepository.findAll().stream().findFirst().orElse(null);
        Auditorium auditorium = auditoriumRepository.findAll().stream().findFirst().orElse(null);
        Category category = categoryRepository.findAll().stream().findFirst().orElse(null);

        if (organization == null || venue == null || auditorium == null || category == null) {
            System.out.println("Data init skipped: missing organization/venue/auditorium/category for analytics demo event");
            return;
        }

        seedPastAnalyticsEvent(organization, venue, auditorium, category);
        System.out.println("Data initialized successfully");
    }

    private void seedPastAnalyticsEvent(
            Organization organization,
            Venue venue,
            Auditorium auditorium,
            Category category
    ) {
        String demoTitle = "Demo Past Event For Analytics";
        boolean alreadyExists = eventRepository.findAll().stream()
                .anyMatch(event -> demoTitle.equals(event.getTitle()));
        if (alreadyExists) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Event event = new Event();
        event.setTitle(demoTitle);
        event.setDescription("Seeded event to demonstrate organizer analytics for a past event.");
        event.setOrganization(organization);
        event.setVenue(venue);
        event.setAuditorium(auditorium);
        event.setCategory(category);
        event.setPerformers(List.of("Demo Band", "Guest Artist"));
        event.setImages(List.of("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f"));
        event.setStatus(Event.EventStatus.COMPLETED);
        event.setStartDatetime(now.minusDays(20));
        event.setEndDatetime(now.minusDays(20).plusHours(3));
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        eventRepository.save(event);

        TicketType vip = new TicketType();
        vip.setEvent(event);
        vip.setName("VIP");
        vip.setPrice(new BigDecimal("120.00"));
        vip.setTotalQuantity(50);
        vip.setAvailableQuantity(35);
        ticketTypeRepository.save(vip);

        TicketType regular = new TicketType();
        regular.setEvent(event);
        regular.setName("Regular");
        regular.setPrice(new BigDecimal("40.00"));
        regular.setTotalQuantity(120);
        regular.setAvailableQuantity(80);
        ticketTypeRepository.save(regular);

        Purchase vipPurchase = new Purchase();
        vipPurchase.setBuyerName("VIP Buyer");
        vipPurchase.setBuyerEmail("vip-buyer@example.com");
        vipPurchase.setTotalAmount(new BigDecimal("1800.00"));
        vipPurchase.setCurrency("EUR");
        vipPurchase.setPaymentProvider("demo");
        vipPurchase.setProviderTransactionId("demo-vip-" + event.getEventId());
        vipPurchase.setStatus(Purchase.PurchaseStatus.COMPLETED);
        vipPurchase.setCreatedAt(now.minusDays(21));
        purchaseRepository.save(vipPurchase);

        Purchase regularPurchase = new Purchase();
        regularPurchase.setBuyerName("Regular Buyer");
        regularPurchase.setBuyerEmail("regular-buyer@example.com");
        regularPurchase.setTotalAmount(new BigDecimal("1600.00"));
        regularPurchase.setCurrency("EUR");
        regularPurchase.setPaymentProvider("demo");
        regularPurchase.setProviderTransactionId("demo-regular-" + event.getEventId());
        regularPurchase.setStatus(Purchase.PurchaseStatus.COMPLETED);
        regularPurchase.setCreatedAt(now.minusDays(21));
        purchaseRepository.save(regularPurchase);

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Ticket ticket = new Ticket();
            ticket.setPurchase(vipPurchase);
            ticket.setTicketType(vip);
            ticket.setQrToken("demo-" + event.getEventId() + "-vip-" + i);
            ticket.setStatus(i <= 12 ? Ticket.TicketStatus.CHECKED_IN : Ticket.TicketStatus.VALID);
            ticket.setIssuedAt(now.minusDays(21));
            tickets.add(ticket);
        }

        for (int i = 1; i <= 40; i++) {
            Ticket ticket = new Ticket();
            ticket.setPurchase(regularPurchase);
            ticket.setTicketType(regular);
            ticket.setQrToken("demo-" + event.getEventId() + "-regular-" + i);
            ticket.setStatus(i <= 30 ? Ticket.TicketStatus.CHECKED_IN : Ticket.TicketStatus.VALID);
            ticket.setIssuedAt(now.minusDays(21));
            tickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        List<CheckIn> checkIns = savedTickets.stream()
                .filter(ticket -> ticket.getStatus() == Ticket.TicketStatus.CHECKED_IN)
                .map(ticket -> {
                    CheckIn checkIn = new CheckIn();
                    checkIn.setTicket(ticket);
                    checkIn.setScannedAt(now.minusDays(20).plusHours(1));
                    checkIn.setScanResult(CheckIn.ScanResult.SUCCESS);
                    return checkIn;
                })
                .toList();
        checkInRepository.saveAll(checkIns);
    }
}