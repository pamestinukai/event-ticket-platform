package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.response.VenueResponseDTO;
import com.pamestinukai.backend.mappers.VenueResponseMapper;
import com.pamestinukai.backend.repositories.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueRepository venueRepository;
    private final VenueResponseMapper venueResponseMapper;

    @GetMapping
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        List<VenueResponseDTO> venues = venueRepository.findAll().stream()
                .map(venueResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(venues);
    }
}
