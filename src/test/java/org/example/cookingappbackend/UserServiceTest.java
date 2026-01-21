package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.request.UpdateProfileRequest;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService service = new UserService(userRepository);

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    @Test
    void loadUserByUsername_returnsUser() {
        User u = user(1L, "a@test.com");

        when(userRepository.findByEmail("a@test.com"))
                .thenReturn(Optional.of(u));

        var result = service.loadUserByUsername("a@test.com");

        assertThat(result).isSameAs(u);
        verify(userRepository).findByEmail("a@test.com");
    }

    @Test
    void loadUserByUsername_whenNotFound_throws() {
        when(userRepository.findByEmail("x@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail("x@test.com");
    }

    @Test
    void findByEmail_returnsUser() {
        User u = user(2L, "b@test.com");

        when(userRepository.findByEmail("b@test.com"))
                .thenReturn(Optional.of(u));

        User result = service.findByEmail("b@test.com");

        assertThat(result).isSameAs(u);
        verify(userRepository).findByEmail("b@test.com");
    }

    @Test
    void findByEmail_whenNotFound_throws() {
        when(userRepository.findByEmail("y@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("y@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail("y@test.com");
    }

    @Test
    void updateProfile_updatesNameAndSurname() {
        User current = user(1L, "a@test.com");

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("a@test.com");
        req.setName("Jan");
        req.setSurname("Kowalski");

        when(userRepository.save(current)).thenReturn(current);

        User result = service.updateProfile(current, req);

        assertThat(result.getName()).isEqualTo("Jan");
        assertThat(result.getSurname()).isEqualTo("Kowalski");
        assertThat(result.getEmail()).isEqualTo("a@test.com");

        verify(userRepository).save(current);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateProfile_changesEmail_whenNotTaken() {
        User current = user(1L, "old@test.com");

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("new@test.com");
        req.setName("Jan");
        req.setSurname("Nowak");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(current)).thenReturn(current);

        User result = service.updateProfile(current, req);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getName()).isEqualTo("Jan");
        assertThat(result.getSurname()).isEqualTo("Nowak");

        verify(userRepository).existsByEmail("new@test.com");
        verify(userRepository).save(current);
    }

    @Test
    void updateProfile_whenEmailTaken_throwsConflict() {
        User current = user(1L, "old@test.com");

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("taken@test.com");
        req.setName("Jan");
        req.setSurname("Nowak");

        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfile(current, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e ->
                        assertThat(((ResponseStatusException) e).getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT)
                );

        verify(userRepository).existsByEmail("taken@test.com");
        verify(userRepository, never()).save(any());
    }
}
