package org.example.cookingappbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.PantryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PantryController.class,
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
class PantryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PantryService pantryService;

    @MockBean
    private org.example.cookingappbackend.service.JwtService jwtService;

    @MockBean
    private org.example.cookingappbackend.service.UserService userService;

    @Test
    void list_callsService() throws Exception {
        User currentUser = new User();
        var auth = new UsernamePasswordAuthenticationToken(
                currentUser,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(pantryService.list(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/pantry")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(pantryService).list(any());
        verifyNoMoreInteractions(pantryService);
    }

    @Test
    void upsert_callsServiceWithBody() throws Exception {
        User currentUser = new User();
        var auth = new UsernamePasswordAuthenticationToken(
                currentUser,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Long ingredientId = 5L;
        Double amount = 2.5;

        String json = """
                {"ingredientId":%d,"amount":%s}
                """.formatted(ingredientId, amount);

        when(pantryService.upsert(any(), anyLong(), anyDouble())).thenReturn(null);

        mockMvc.perform(post("/api/pantry")
                        .with(authentication(auth))
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(pantryService).upsert(any(), eq(ingredientId), eq(amount));
        verifyNoMoreInteractions(pantryService);
    }

    @Test
    void delete_callsServiceWithPathVariable() throws Exception {
        User currentUser = new User();
        var auth = new UsernamePasswordAuthenticationToken(
                currentUser,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Long id = 12L;

        mockMvc.perform(delete("/api/pantry/{id}", id)
                        .with(authentication(auth)))
                .andExpect(status().isOk());

        verify(pantryService).delete(any(), eq(id));
        verifyNoMoreInteractions(pantryService);
    }
}
