package org.example.cookingappbackend.dto.response;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class IngredientResponse {
    private Long id;

    @NotBlank @Size(max = 120)
    private String name;

    @NotBlank @Size(max = 20)
    private String unit;
}
