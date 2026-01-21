package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.request.ChangePasswordRequest;
import org.example.cookingappbackend.dto.request.LoginRequest;
import org.example.cookingappbackend.dto.request.RegisterRequest;
import org.example.cookingappbackend.dto.response.AuthResponse;
import org.example.cookingappbackend.enums.Role;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;


    @Test
    void register_shouldThrow400_whenPasswordIsNull() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@a.pl");
        req.setPassword(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrow400_whenPasswordTooShort() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@a.pl");
        req.setPassword("12345"); // < 6

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrow409_whenEmailAlreadyTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("taken@a.pl");
        req.setPassword("123456");

        when(userRepository.existsByEmail("taken@a.pl")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository).existsByEmail("taken@a.pl");
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void register_shouldSaveUserWithEncodedPasswordAndUserRole_andReturnTokens() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Jan");
        req.setSurname("Kowalski");
        req.setEmail("jan@a.pl");
        req.setPassword("123456");

        when(userRepository.existsByEmail("jan@a.pl")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("ENC_PASS");
        when(jwtService.generateToken(any(User.class))).thenReturn("ACCESS");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("REFRESH");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertTokenPair(res, "ACCESS", "REFRESH");

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("Jan", saved.getName());
        assertEquals("Kowalski", saved.getSurname());
        assertEquals("jan@a.pl", saved.getEmail());
        assertEquals("ENC_PASS", saved.getPassword());
        assertNotNull(saved.getCreatedAt());
        assertEquals(Set.of(Role.USER), saved.getRoles());

        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }


    @Test
    void login_shouldAuthenticateThenReturnTokens() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@a.pl");
        req.setPassword("pass123");

        User user = new User();
        user.setEmail("user@a.pl");

        when(userRepository.findByEmail("user@a.pl")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("A");
        when(jwtService.generateRefreshToken(user)).thenReturn("R");

        AuthResponse res = authService.login(req);

        assertTokenPair(res, "A", "R");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken token = authCaptor.getValue();

        assertEquals("user@a.pl", token.getPrincipal());
        assertEquals("pass123", token.getCredentials());

        verify(userRepository).findByEmail("user@a.pl");
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void login_shouldThrow_whenUserNotFoundAfterAuthentication() {
        LoginRequest req = new LoginRequest();
        req.setEmail("missing@a.pl");
        req.setPassword("pass123");

        when(userRepository.findByEmail("missing@a.pl")).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> authService.login(req));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("missing@a.pl");
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }


    @Test
    void changePassword_shouldThrow400_whenCurrentPasswordInvalid() {
        User currentUser = new User();
        currentUser.setPassword("HASH");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newpass123");

        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.changePassword(currentUser, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrow400_whenNewPasswordTooShort() {
        User currentUser = new User();
        currentUser.setPassword("HASH");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("ok");
        req.setNewPassword("12345"); // < 6

        when(passwordEncoder.matches("ok", "HASH")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.changePassword(currentUser, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldEncodeAndSaveNewPassword() {
        User currentUser = new User();
        currentUser.setPassword("OLD_HASH");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newpass123");

        when(passwordEncoder.matches("old", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("NEW_HASH");

        authService.changePassword(currentUser, req);

        assertEquals("NEW_HASH", currentUser.getPassword());
        verify(userRepository).save(currentUser);
    }


    @Test
    void refreshToken_shouldThrow401_whenExtractUsernameFails() {
        when(jwtService.extractUsername("bad")).thenThrow(new RuntimeException("bad"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.refreshToken("bad"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).isRefreshTokenValid(anyString(), any());
    }

    @Test
    void refreshToken_shouldThrow401_whenUserNotFound() {
        when(jwtService.extractUsername("rt")).thenReturn("x@a.pl");
        when(userRepository.findByEmail("x@a.pl")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.refreshToken("rt"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(jwtService, never()).isRefreshTokenValid(anyString(), any());
    }

    @Test
    void refreshToken_shouldThrow401_whenRefreshTokenInvalidForUser() {
        User user = new User();
        user.setEmail("u@a.pl");

        when(jwtService.extractUsername("rt")).thenReturn("u@a.pl");
        when(userRepository.findByEmail("u@a.pl")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("rt", user)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.refreshToken("rt"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(jwtService).isRefreshTokenValid("rt", user);
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenValid() {
        User user = new User();
        user.setEmail("u@a.pl");

        when(jwtService.extractUsername("rt")).thenReturn("u@a.pl");
        when(userRepository.findByEmail("u@a.pl")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("rt", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("NEW_ACCESS");
        when(jwtService.generateRefreshToken(user)).thenReturn("NEW_REFRESH");

        AuthResponse res = authService.refreshToken("rt");

        assertTokenPair(res, "NEW_ACCESS", "NEW_REFRESH");
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    private static void assertTokenPair(AuthResponse res, String access, String refresh) {
        assertNotNull(res);


        try {
            assertEquals(access, res.getAccessToken());
            assertEquals(refresh, res.getRefreshToken());
            return;
        } catch (Throwable ignored) {

            fail("Dopasuj asercje w assertTokenPair() do pól/getterów AuthResponse.");
        }
    }
}
