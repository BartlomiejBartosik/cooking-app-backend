package org.example.cookingappbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RatingController.class,
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
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private org.example.cookingappbackend.service.JwtService jwtService;

    @MockBean
    private org.example.cookingappbackend.service.UserService userService;

    @Test
    void getRatings_callsService() throws Exception {
        when(ratingService.getRatings(eq(10L), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/recipes/{recipeId}/rating", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(ratingService).getRatings(eq(10L), any());
        verifyNoMoreInteractions(ratingService);
    }

    @Test
    void upsertRating_returnsNoContent_andCallsService() throws Exception {
        String json = """
                {"value":5}
                """;

        mockMvc.perform(post("/api/recipes/{recipeId}/rating", 10L)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        verify(ratingService).upsertRating(eq(10L), any(), any());
        verifyNoMoreInteractions(ratingService);
    }

    @Test
    void deleteMyRating_returnsNoContent_andCallsService() throws Exception {
        mockMvc.perform(delete("/api/recipes/{recipeId}/rating", 10L))
                .andExpect(status().isNoContent());

        verify(ratingService).deleteMyRating(eq(10L), any());
        verifyNoMoreInteractions(ratingService);
    }
}
