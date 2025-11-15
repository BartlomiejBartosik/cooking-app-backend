package org.example.cookingappbackend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShoppingListItemRequest {
    private Long ingredientId;
    private String name;
    private Double amount;
    private String unit;
}

