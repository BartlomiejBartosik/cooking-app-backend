package org.example.cookingappbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cookingappbackend.dto.request.AddFromRecipeRequest;
import org.example.cookingappbackend.dto.request.ShoppingListItemRequest;
import org.example.cookingappbackend.dto.response.ShoppingListDetailsResponse;
import org.example.cookingappbackend.dto.response.ShoppingListSummaryResponse;
import org.example.cookingappbackend.service.ShoppingListService;
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
        controllers = ShoppingListController.class,
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
class ShoppingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingListService shoppingListService;

    @MockBean
    private org.example.cookingappbackend.service.JwtService jwtService;

    @MockBean
    private org.example.cookingappbackend.service.UserService userService;

    @Test
    void list_callsService() throws Exception {
        when(shoppingListService.list(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/shopping-lists"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(shoppingListService).list(any());
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void create_withoutName_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.create(any(), isNull())).thenReturn(resp);

        mockMvc.perform(post("/api/shopping-lists"))
                .andExpect(status().isOk());

        verify(shoppingListService).create(any(), isNull());
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void create_withName_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.create(any(), eq("Zakupy"))).thenReturn(resp);

        mockMvc.perform(post("/api/shopping-lists")
                        .param("name", "Zakupy"))
                .andExpect(status().isOk());

        verify(shoppingListService).create(any(), eq("Zakupy"));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void get_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.get(any(), eq(10L))).thenReturn(resp);

        mockMvc.perform(get("/api/shopping-lists/{listId}", 10L))
                .andExpect(status().isOk());

        verify(shoppingListService).get(any(), eq(10L));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void addItem_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.addItem(any(), eq(10L), any(ShoppingListItemRequest.class))).thenReturn(resp);

        String json = """
                {"name":"Milk","amount":2.0,"unit":"l","category":"DAIRY"}
                """;

        mockMvc.perform(post("/api/shopping-lists/{listId}/items", 10L)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(shoppingListService).addItem(any(), eq(10L), any(ShoppingListItemRequest.class));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void updateItem_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.updateItem(any(), eq(10L), eq(5L), any(ShoppingListItemRequest.class))).thenReturn(resp);

        String json = """
                {"name":"Milk","amount":3.0,"unit":"l","category":"DAIRY"}
                """;

        mockMvc.perform(put("/api/shopping-lists/{listId}/items/{itemId}", 10L, 5L)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(shoppingListService).updateItem(any(), eq(10L), eq(5L), any(ShoppingListItemRequest.class));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void deleteItem_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.deleteItem(any(), eq(10L), eq(5L))).thenReturn(resp);

        mockMvc.perform(delete("/api/shopping-lists/{listId}/items/{itemId}", 10L, 5L))
                .andExpect(status().isOk());

        verify(shoppingListService).deleteItem(any(), eq(10L), eq(5L));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void finalizeList_defaultAddToPantryFalse_callsService() throws Exception {
        mockMvc.perform(post("/api/shopping-lists/{id}/finalize", 10L))
                .andExpect(status().isOk());

        verify(shoppingListService).finalizeList(any(), eq(10L), eq(false));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void finalizeList_addToPantryTrue_callsService() throws Exception {
        mockMvc.perform(post("/api/shopping-lists/{id}/finalize", 10L)
                        .param("addToPantry", "true"))
                .andExpect(status().isOk());

        verify(shoppingListService).finalizeList(any(), eq(10L), eq(true));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void addFromRecipe_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.addFromRecipe(any(), eq(7L), any(AddFromRecipeRequest.class))).thenReturn(resp);

        String json = """
                {"listId":10}
                """;

        mockMvc.perform(post("/api/shopping-lists/add-from-recipe/{recipeId}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(shoppingListService).addFromRecipe(any(), eq(7L), any(AddFromRecipeRequest.class));
        verifyNoMoreInteractions(shoppingListService);
    }

    @Test
    void rename_callsService() throws Exception {
        ShoppingListDetailsResponse resp = mock(ShoppingListDetailsResponse.class);
        when(shoppingListService.rename(any(), eq(10L), eq("Nowa nazwa"))).thenReturn(resp);

        String json = """
                {"name":"Nowa nazwa"}
                """;

        mockMvc.perform(patch("/api/shopping-lists/{id}", 10L)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(shoppingListService).rename(any(), eq(10L), eq("Nowa nazwa"));
        verifyNoMoreInteractions(shoppingListService);
    }
}
