package org.example.cookingappbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingListItemResponse {

    private Long id;
    private Long ingredientId;
    private String name;
    private Double amount;
    private String unit;
}