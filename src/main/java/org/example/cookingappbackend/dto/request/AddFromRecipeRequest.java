package org.example.cookingappbackend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddFromRecipeRequest {
    private String mode;
    private Long listId;
    private String newListName;
}
