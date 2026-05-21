package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.CategoryResponseDTO;
import com.pamestinukai.backend.entities.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryResponseMapper {
    public CategoryResponseDTO toDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
