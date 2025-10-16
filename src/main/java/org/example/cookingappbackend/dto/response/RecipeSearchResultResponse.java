package org.example.cookingappbackend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecipeSearchResultResponse {
    private Long id;
    private String title;
    private int matchedCount;
    private int missingCount;
}

