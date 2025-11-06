package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.ChangePasswordRequest;
import org.example.cookingappbackend.dto.request.LoginRequest;
import org.example.cookingappbackend.dto.request.RegisterRequest;
import org.example.cookingappbackend.dto.response.AuthResponse;
import org.example.cookingappbackend.enums.Role;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }
        User u = new User();
        u.setName(req.getName());
        u.setSurname(req.getSurname());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setCreatedAt(LocalDateTime.now());
        u.setRoles(Set.of(Role.USER));
        userRepository.save(u);

        String access = jwtService.generateToken(u);
        String refresh = jwtService.generateRefreshToken(u);
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User u = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String access = jwtService.generateToken(u);
        String refresh = jwtService.generateRefreshToken(u);
        return new AuthResponse(access, refresh);
    }


    public void changePassword(User currentUser, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), currentUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nieprawidłowe obecne hasło.");
        }

        if (req.getNewPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nowe hasło musi mieć min. 6 znaków.");
        }

        currentUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(currentUser);
    }
}
