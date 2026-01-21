package org.example.cookingappbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.config.JwtAuthFilter;
import org.example.cookingappbackend.controller.AuthController;
import org.example.cookingappbackend.dto.response.AuthResponse;
import org.example.cookingappbackend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_shouldReturn200() throws Exception {
        when(authService.register(any()))
                .thenReturn(new AuthResponse("A", "R"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "name":"Jan",
                      "surname":"Kowalski",
                      "email":"a@a.pl",
                      "password":"123456"
                    }
                """))
                .andExpect(status().isOk());
    }


    @Test
    void login_shouldReturn200() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("A", "R"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@a.pl","password":"123456"}
                                """))
                .andDo(print())
                .andExpect(status().isOk());

        verify(authService).login(any());
    }

    @Test
    void refresh_shouldReturn200_andPassToken() throws Exception {
        when(authService.refreshToken(eq("rt-123"))).thenReturn(new AuthResponse("NA", "NR"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"rt-123"}
                                """))
                .andDo(print())
                .andExpect(status().isOk());

        verify(authService).refreshToken("rt-123");
    }
}
