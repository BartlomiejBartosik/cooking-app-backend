package org.example.cookingappbackend.dto.response;

import org.example.cookingappbackend.model.Recipe;

public record RecipeSummaryResponse(Long id, String title, Integer time, Double rating) {
    public static RecipeSummaryResponse from(Recipe r) {
        return new RecipeSummaryResponse(
                r.getId(),
                r.getTitle(),
                r.getTotalTimeMin(),
                r.getAvgRating()
        );
    }
}
