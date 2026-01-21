package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.UpdateProfileRequest;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User updateProfile(User currentUser, UpdateProfileRequest req) {
        if (!currentUser.getEmail().equalsIgnoreCase(req.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email jest już używany.");
            }
            currentUser.setEmail(req.getEmail());
        }
        currentUser.setName(req.getName());
        currentUser.setSurname(req.getSurname());
        return userRepository.save(currentUser);
    }
}
