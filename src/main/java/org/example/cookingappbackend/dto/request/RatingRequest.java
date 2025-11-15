package org.example.cookingappbackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingRequest {
        @NotNull
        @Min(1)
        @Max(5)
        Integer stars;
        String comment;
}
