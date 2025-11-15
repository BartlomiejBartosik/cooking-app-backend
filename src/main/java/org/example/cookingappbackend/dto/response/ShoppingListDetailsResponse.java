package org.example.cookingappbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListDetailsResponse {
    private Long id;
    private String name;
    private List<ShoppingListItemResponse> items;
}

