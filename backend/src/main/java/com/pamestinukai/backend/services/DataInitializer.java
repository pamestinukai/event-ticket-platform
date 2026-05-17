package com.pamestinukai.backend.services;

import com.pamestinukai.backend.entities.*;
import com.pamestinukai.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        boolean manualInit = List.of(args).contains("--init-data");

        if (!manualInit) {
            return;
        }

        // if there are rows in events repository then initialization is skipped
        if (eventRepository.count() > 0) return;

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

        System.out.println("Data initialized successfully");
    }
}