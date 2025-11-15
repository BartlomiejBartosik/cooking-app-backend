package org.example.cookingappbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingResponse {
    private Long id;
    private String userName;
    private Integer stars;
    private String comment;
    private LocalDateTime createdAt;
    private boolean mine;
}
