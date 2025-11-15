package org.example.cookingappbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingListSummaryResponse {
    private Long id;
    private String name;
    private int itemsCount;
}
