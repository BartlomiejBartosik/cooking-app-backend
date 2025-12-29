package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.LoginRequest;
import org.example.cookingappbackend.dto.request.RegisterRequest;
import org.example.cookingappbackend.dto.response.AuthResponse;
import org.example.cookingappbackend.service.AuthService;
import org.example.cookingappbackend.dto.request.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }
}
