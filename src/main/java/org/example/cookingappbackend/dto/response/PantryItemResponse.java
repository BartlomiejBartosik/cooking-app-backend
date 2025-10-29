package org.example.cookingappbackend.dto.response;

public record PantryItemResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        String ingredientCategory,
        String ingredientUnit,
        Double amount
) {}
