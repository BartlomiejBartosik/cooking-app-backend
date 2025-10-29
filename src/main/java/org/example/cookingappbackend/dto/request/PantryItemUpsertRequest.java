package org.example.cookingappbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PantryItemUpsertRequest{
        @NotNull Long ingredientId;
        Double amount;
}
