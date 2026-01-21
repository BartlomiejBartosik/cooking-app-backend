package org.example.cookingappbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.dto.response.RecipeSummaryResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.FavoriteService;
import org.example.cookingappbackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FavoriteController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                org.example.cookingappbackend.config.SecurityConfig.class,
                                org.example.cookingappbackend.config.JwtAuthFilter.class
                        }
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@example.com")
    void addFavorite_shouldReturnFavoriteTrue_andCallService() throws Exception {
        long recipeId = 10L;
        User user = new User();
        user.setId(7L);

        given(userService.findByEmail("test@example.com")).willReturn(user);

        mockMvc.perform(post("/api/recipes/{id}/favorite", recipeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.favorite").value(true));

        verify(userService).findByEmail("test@example.com");
        verify(favoriteService).add(7L, recipeId);
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeFavorite_shouldReturnFavoriteFalse_andCallService() throws Exception {
        long recipeId = 11L;
        User user = new User();
        user.setId(8L);

        given(userService.findByEmail("test@example.com")).willReturn(user);

        mockMvc.perform(delete("/api/recipes/{id}/favorite", recipeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.favorite").value(false));

        verify(userService).findByEmail("test@example.com");
        verify(favoriteService).remove(8L, recipeId);
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void listFavorites_defaults_shouldReturnPage_andPassDefaultPageable() throws Exception {
        User user = new User();
        user.setId(9L);
        given(userService.findByEmail("test@example.com")).willReturn(user);

        RecipeSummaryResponse r1 = mock(RecipeSummaryResponse.class);
        RecipeSummaryResponse r2 = mock(RecipeSummaryResponse.class);

        Page<RecipeSummaryResponse> page = new PageImpl<>(
                List.of(r1, r2),
                PageRequest.of(0, 20),
                2
        );

        given(favoriteService.list(eq(9L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/favorites")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(favoriteService).list(eq(9L), pageableCaptor.capture());
        Pageable passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(0);
        assertThat(passed.getPageSize()).isEqualTo(20);

        verify(userService).findByEmail("test@example.com");
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void listFavorites_customPage_shouldPassCustomPageable() throws Exception {
        User user = new User();
        user.setId(15L);
        given(userService.findByEmail("test@example.com")).willReturn(user);

        Page<RecipeSummaryResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(2, 5),
                0
        );
        given(favoriteService.list(eq(15L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/favorites")
                        .param("page", "2")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(2));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(favoriteService).list(eq(15L), pageableCaptor.capture());
        Pageable passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(2);
        assertThat(passed.getPageSize()).isEqualTo(5);

        verify(userService).findByEmail("test@example.com");
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void ids_shouldReturnIdsList_andCallService() throws Exception {
        User user = new User();
        user.setId(21L);
        given(userService.findByEmail("test@example.com")).willReturn(user);

        given(favoriteService.ids(21L)).willReturn(List.of(1L, 2L, 5L));

        mockMvc.perform(get("/api/favorites/ids")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2))
                .andExpect(jsonPath("$[2]").value(5));

        verify(userService).findByEmail("test@example.com");
        verify(favoriteService).ids(21L);
        verifyNoMoreInteractions(favoriteService);
    }


}
