package org.example.cookingappbackend.controller;

import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = IngredientController.class,
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
class IngredientControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngredientRepository ingredientRepo;

    @MockBean
    private org.example.cookingappbackend.service.JwtService jwtService;

    @MockBean
    org.example.cookingappbackend.service.UserService userService;

    @Test
    void search_withoutQuery_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void search_blankQuery_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/ingredients")
                        .param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void search_trimsAndLowercasesQuery() throws Exception {
        Ingredient ing = mockIngredient(1L, "Milk", "ml");
        given(ingredientRepo.searchRanked(anyString())).willReturn(List.of(ing));

        mockMvc.perform(get("/api/ingredients")
                        .param("query", "  MiLk  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Milk"))
                .andExpect(jsonPath("$[0].unit").value("ml"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ingredientRepo).searchRanked(captor.capture());
        assertThat(captor.getValue()).isEqualTo("milk");
    }

    @Test
    void search_defaultLimit_truncatesTo20() throws Exception {
        List<Ingredient> many = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            many.add(mockIngredient((long) i, "Ing" + i, "g"));
        }
        given(ingredientRepo.searchRanked("x")).willReturn(many);

        mockMvc.perform(get("/api/ingredients")
                        .param("query", "x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20));

        verify(ingredientRepo).searchRanked("x");
    }

    @Test
    void search_customLimit_truncates() throws Exception {
        List<Ingredient> many = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            many.add(mockIngredient((long) i, "Ing" + i, "g"));
        }
        given(ingredientRepo.searchRanked("abc")).willReturn(many);

        mockMvc.perform(get("/api/ingredients")
                        .param("query", "abc")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        verify(ingredientRepo).searchRanked("abc");
    }

    private static Ingredient mockIngredient(Long id, String name, String unit) throws Exception {
        Ingredient ing = mock(Ingredient.class);

        when(ing.getId()).thenReturn(id);
        when(ing.getName()).thenReturn(name);
        when(ing.getUnit()).thenReturn(unit);

        Method m = Ingredient.class.getMethod("getCategory");
        Object firstEnum = m.getReturnType().getEnumConstants()[0];
        doReturn(firstEnum).when(ing).getCategory();

        return ing;
    }
}
