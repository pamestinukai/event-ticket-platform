package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.response.CategoryResponseDTO;
import com.pamestinukai.backend.mappers.CategoryResponseMapper;
import com.pamestinukai.backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final CategoryResponseMapper categoryResponseMapper;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryRepository.findAll().stream()
                .map(categoryResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(categories);
    }
}
