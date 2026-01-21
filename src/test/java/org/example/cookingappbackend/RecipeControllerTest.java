package org.example.cookingappbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.dto.request.RecipeCreateRequest;
import org.example.cookingappbackend.dto.response.RecipeResponse;
import org.example.cookingappbackend.dto.response.RecipeSummaryResponse;
import org.example.cookingappbackend.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RecipeController.class,
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
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private org.example.cookingappbackend.service.JwtService jwtService;

    @MockBean
    private org.example.cookingappbackend.service.UserService userService;

    @Test
    void create_callsService_andReturnsOk() throws Exception {
        RecipeResponse resp = mock(RecipeResponse.class);
        when(recipeService.create(any(RecipeCreateRequest.class), any())).thenReturn(resp);

        String json = """
                {
                  "title": "Test recipe",
                  "description": "Desc",
                  "totalTimeMin": 25,
                  "ingredients": [
                    { "ingredientId": 1, "amount": 2.0 },
                    { "ingredientId": 2, "amount": 1.5 }
                  ],
                  "steps": [
                    { "stepNo": 1, "instruction": "Step one", "timeMin": 5 },
                    { "stepNo": 2, "instruction": "Step two", "timeMin": 10 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/recipes")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(recipeService).create(any(RecipeCreateRequest.class), any());
        verifyNoMoreInteractions(recipeService);
    }

    @Test
    void list_usesDefaultPageable_sortTitle_size20() throws Exception {
        Page<RecipeSummaryResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20, Sort.by("title")),
                0
        );
        when(recipeService.list(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipeService).list(captor.capture());
        Pageable p = captor.getValue();

        assertThat(p.getPageNumber()).isEqualTo(0);
        assertThat(p.getPageSize()).isEqualTo(20);
        assertThat(p.getSort().getOrderFor("title")).isNotNull();

        verifyNoMoreInteractions(recipeService);
    }

    @Test
    void get_callsService_andReturnsOk() throws Exception {
        RecipeResponse resp = mock(RecipeResponse.class);
        when(recipeService.get(10L)).thenReturn(resp);

        mockMvc.perform(get("/api/recipes/{id}", 10L))
                .andExpect(status().isOk());

        verify(recipeService).get(10L);
        verifyNoMoreInteractions(recipeService);
    }

    @Test
    void search_prefersQueryAlias_whenQBlank() throws Exception {
        Page<RecipeSummaryResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(recipeService.search(anyString(), any(), anyBoolean(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/recipes/search")
                        .param("q", "   ")
                        .param("query", "pasta"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        ArgumentCaptor<String> qCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipeService).search(
                qCaptor.capture(),
                any(),
                anyBoolean(),
                any(),
                any(Pageable.class)
        );

        assertThat(qCaptor.getValue()).isEqualTo("pasta");
        verifyNoMoreInteractions(recipeService);
    }

    @Test
    void search_passesIngredientsAndInPantryOnly_andPageableDefaultSize20() throws Exception {
        Page<RecipeSummaryResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(recipeService.search(any(), any(), anyBoolean(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/recipes/search")
                        .param("q", "soup")
                        .param("ingredients", "tomato,onion")
                        .param("inPantryOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipeService).search(
                eq("soup"),
                eq("tomato,onion"),
                eq(true),
                any(),
                pageableCaptor.capture()
        );

        Pageable p = pageableCaptor.getValue();
        assertThat(p.getPageSize()).isEqualTo(20);

        verifyNoMoreInteractions(recipeService);
    }

    @Test
    void listTopRated_callsService_withDefaultSize20() throws Exception {
        Page<RecipeSummaryResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(recipeService.listTopRated(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/recipes/top-rated"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipeService).listTopRated(captor.capture());

        Pageable p = captor.getValue();
        assertThat(p.getPageSize()).isEqualTo(20);

        verifyNoMoreInteractions(recipeService);
    }
}
