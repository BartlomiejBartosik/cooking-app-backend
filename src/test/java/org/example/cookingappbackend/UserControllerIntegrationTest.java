package org.example.cookingappbackend.integration;

import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void me_returnsAuthenticatedUser() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@test.com";

        User u = new User();
        u.setEmail(email);
        u.setName("Jan");
        u.setSurname("Kowalski");
        u.setPassword("x");
        u = userRepository.save(u);

        mockMvc.perform(get("/api/user/me")
                        .with(user(u)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("Jan"))
                .andExpect(jsonPath("$.surname").value("Kowalski"));
    }
}

