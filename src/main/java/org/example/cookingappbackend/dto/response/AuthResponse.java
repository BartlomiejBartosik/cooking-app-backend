package org.example.cookingappbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}
