package com.pamestinukai.backend.services;

import com.pamestinukai.backend.entities.*;
import com.pamestinukai.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
    private final EventRepository eventRepository;

    @Override
    public void run(String... args) {
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
        owner1.setPasswordHash("hashed_password_1");
        owner1.setPhone("+37061111111");
        owner1.setActive(true);
        owner1.setCreatedAt(LocalDateTime.now());
        owner1.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(owner1);

        Employee owner2 = new Employee();
        owner2.setEmail("owner2@eventpro.com");
        owner2.setPasswordHash("hashed_password_2");
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
        event1.setDescription("The biggest rock concert of the year featuring top bands.");
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

        System.out.println("Data initialized successfully");
    }
}