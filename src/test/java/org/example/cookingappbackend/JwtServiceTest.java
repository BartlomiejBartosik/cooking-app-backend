package org.example.cookingappbackend.service;

import org.example.cookingappbackend.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_andExtractUsername_returnsEmail() {
        User user = new User();
        user.setId(123L);
        user.setEmail("test@example.com");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateRefreshToken_andExtractUsername_returnsEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("refresh@example.com");

        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("refresh@example.com");
    }

    @Test
    void isRefreshTokenValid_trueForMatchingUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("valid@example.com");

        String refresh = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isRefreshTokenValid(refresh, user)).isTrue();
    }

    @Test
    void isRefreshTokenValid_falseForDifferentUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@example.com");

        User other = new User();
        other.setId(2L);
        other.setEmail("b@example.com");

        String refresh = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isRefreshTokenValid(refresh, other)).isFalse();
    }

    @Test
    void extractUsername_throwsForInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isRefreshTokenValid_throwsForInvalidToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("x@example.com");

        assertThatThrownBy(() -> jwtService.isRefreshTokenValid("not-a-jwt", user))
                .isInstanceOf(Exception.class);
    }
}
