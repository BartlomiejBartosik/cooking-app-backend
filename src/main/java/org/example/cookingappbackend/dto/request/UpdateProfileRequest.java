package org.example.cookingappbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank
    private String name;

    private String surname;

    @NotBlank
    @Email
    private String email;
}
