package org.example.cookingappbackend.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class RecipeResponse {
    private Long id;
    private String title;
    private String description;
    private Integer totalTimeMin;
    private Double avgRating;
    private List<IngredientLine> ingredients;
    private List<StepLine> steps;

    @Data
    public static class IngredientLine {
        private Long ingredientId;
        private String ingredientName;
        private String unit;
        private Double amount;
    }
    @Data
    public static class StepLine {
        private Integer stepNo;
        private String instruction;
        private Integer timeMin;
    }
}