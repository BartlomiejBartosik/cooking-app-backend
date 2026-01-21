package org.example.cookingappbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.dto.request.ChangePasswordRequest;
import org.example.cookingappbackend.dto.request.UpdateProfileRequest;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.AuthService;
import org.example.cookingappbackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                org.example.cookingappbackend.controller.FavoriteController.class,
                                org.example.cookingappbackend.config.SecurityConfig.class
                        }
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private org.example.cookingappbackend.service.JwtService jwtService;

    @Test
    void me_returnsOk() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_callsService_andReturnsOk() throws Exception {
        User updated = new User();
        when(userService.updateProfile(any(User.class), any(UpdateProfileRequest.class))).thenReturn(updated);

        String json = """
            {
              "name": "Test User",
              "email": "test@example.com"
            }
            """;

        mockMvc.perform(put("/api/user/profile")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userService).updateProfile(isNull(), any(UpdateProfileRequest.class));
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(authService);
    }

    @Test
    void changePassword_returnsNoContent_andCallsService() throws Exception {
        String json = """
            {
              "currentPassword": "old123",
              "newPassword": "new12345"
            }
            """;

        mockMvc.perform(put("/api/user/password")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        verify(authService).changePassword(isNull(), any(ChangePasswordRequest.class));
        verifyNoMoreInteractions(authService);
        verifyNoInteractions(userService);
    }
}
