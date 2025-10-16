package org.example.cookingappbackend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class RecipeCreateRequest {
    @NotBlank @Size(max = 180)
    private String title;

    private String description;

    @Positive @Min(1)
    private Integer totalTimeMin;

    @NotNull @Size(min = 1)
    private List<Item> ingredients;

    @NotNull @Size(min = 1)
    private List<Step> steps;

    @Data
    public static class Item {
        @NotNull
        private Long ingredientId;
        @NotNull @Positive
        private Double amount;
    }

    @Data
    public static class Step {
        @NotNull @Positive
        private Integer stepNo;
        @NotBlank
        private String instruction;
        @Positive
        private Integer timeMin;
    }
}

