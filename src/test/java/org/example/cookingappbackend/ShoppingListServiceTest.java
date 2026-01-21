package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.request.AddFromRecipeRequest;
import org.example.cookingappbackend.dto.request.ShoppingListItemRequest;
import org.example.cookingappbackend.dto.response.ShoppingListDetailsResponse;
import org.example.cookingappbackend.dto.response.ShoppingListSummaryResponse;
import org.example.cookingappbackend.model.*;
import org.example.cookingappbackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    private final ShoppingListRepository listRepo = mock(ShoppingListRepository.class);
    private final ShoppingListItemRepository itemRepo = mock(ShoppingListItemRepository.class);
    private final IngredientRepository ingredientRepo = mock(IngredientRepository.class);
    private final RecipeRepository recipeRepo = mock(RecipeRepository.class);
    private final PantryItemRepository pantryItemRepo = mock(PantryItemRepository.class);
    private final PantryService pantryService = mock(PantryService.class);
    private final ShoppingListRepository shoppingListRepository = mock(ShoppingListRepository.class);

    private final ShoppingListService service =
            new ShoppingListService(
                    listRepo,
                    itemRepo,
                    ingredientRepo,
                    recipeRepo,
                    pantryItemRepo,
                    pantryService,
                    shoppingListRepository
            );

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void list_whenUnauthorized_throws401() {
        assertThatThrownBy(() -> service.list(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e ->
                        assertThat(((ResponseStatusException) e).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED)
                );
    }

    @Test
    void list_returnsSummaries() {
        User u = user(1L);

        ShoppingList l1 = new ShoppingList();
        l1.setId(10L);
        l1.setName("A");
        l1.setItems(List.of(new ShoppingListItem()));

        ShoppingList l2 = new ShoppingList();
        l2.setId(11L);
        l2.setName("B");
        l2.setItems(List.of());

        when(listRepo.findByUserOrderByCreatedAtDesc(u))
                .thenReturn(List.of(l1, l2));

        List<ShoppingListSummaryResponse> res = service.list(u);

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getItemsCount()).isEqualTo(1);
        assertThat(res.get(1).getItemsCount()).isEqualTo(0);

        verify(listRepo).findByUserOrderByCreatedAtDesc(u);
    }

    @Test
    void create_generatesUniqueName() {
        User u = user(1L);

        when(listRepo.existsByUserAndNameIgnoreCase(u, "Lista zakupów"))
                .thenReturn(true);
        when(listRepo.existsByUserAndNameIgnoreCase(u, "Lista zakupów (2)"))
                .thenReturn(false);

        ShoppingListDetailsResponse res = service.create(u, null);

        ArgumentCaptor<ShoppingList> captor = ArgumentCaptor.forClass(ShoppingList.class);
        verify(listRepo).save(captor.capture());

        assertThat(captor.getValue().getName())
                .isEqualTo("Lista zakupów (2)");
        assertThat(res.getName()).isEqualTo("Lista zakupów (2)");
    }

    @Test
    void get_whenNotOwner_throws404() {
        User u = user(1L);

        when(listRepo.findByIdAndUser(10L, u))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(u, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e ->
                        assertThat(((ResponseStatusException) e).getStatusCode())
                                .isEqualTo(HttpStatus.NOT_FOUND)
                );
    }

    @Test
    void addItem_withIngredient() {
        User u = user(1L);

        ShoppingList list = new ShoppingList();
        list.setId(10L);
        list.setUser(u);
        list.setItems(new java.util.ArrayList<>());

        Ingredient ing = new Ingredient();
        ing.setId(5L);
        ing.setName("Sugar");

        when(listRepo.findByIdAndUser(10L, u)).thenReturn(Optional.of(list));
        when(ingredientRepo.findById(5L)).thenReturn(Optional.of(ing));

        ShoppingListItemRequest req = new ShoppingListItemRequest();
        req.setIngredientId(5L);
        req.setAmount(2.0);
        req.setUnit("kg");

        ShoppingListDetailsResponse res = service.addItem(u, 10L, req);

        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).getName()).isEqualTo("Sugar");

        verify(itemRepo).save(any());
    }

    @Test
    void updateItem_updatesAmountAndUnit() {
        User u = user(1L);

        ShoppingList list = new ShoppingList();
        list.setId(10L);
        list.setUser(u);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(20L);
        item.setShoppingList(list);
        item.setAmount(1.0);
        item.setUnit("kg");

        list.setItems(new ArrayList<>(List.of(item))); // ← KLUCZOWE

        when(listRepo.findByIdAndUser(10L, u)).thenReturn(Optional.of(list));
        when(itemRepo.findByIdAndShoppingList(20L, list))
                .thenReturn(Optional.of(item));

        ShoppingListItemRequest req = new ShoppingListItemRequest();
        req.setAmount(3.0);
        req.setUnit("g");

        ShoppingListDetailsResponse res = service.updateItem(u, 10L, 20L, req);

        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).getAmount()).isEqualTo(3.0);
        assertThat(res.getItems().get(0).getUnit()).isEqualTo("g");

        verify(itemRepo).save(item);
    }

    @Test
    void deleteItem_removesItem() {
        User u = user(1L);

        ShoppingList list = new ShoppingList();
        list.setId(10L);
        list.setUser(u);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(20L);
        item.setShoppingList(list);

        list.setItems(new java.util.ArrayList<>(List.of(item)));

        when(listRepo.findByIdAndUser(10L, u)).thenReturn(Optional.of(list));
        when(itemRepo.findByIdAndShoppingList(20L, list))
                .thenReturn(Optional.of(item));

        ShoppingListDetailsResponse res = service.deleteItem(u, 10L, 20L);

        assertThat(res.getItems()).isEmpty();
        verify(itemRepo).delete(item);
    }

    @Test
    void rename_changesName() {
        User u = user(1L);

        ShoppingList list = new ShoppingList();
        list.setId(10L);
        list.setUser(u);
        list.setName("Old");

        when(listRepo.findByIdAndUser(10L, u))
                .thenReturn(Optional.of(list));

        ShoppingListDetailsResponse res =
                service.rename(u, 10L, "New");

        assertThat(res.getName()).isEqualTo("New");
        verify(listRepo).save(list);
    }

    @Test
    void finalizeList_withAddToPantry_mergesAndDeletes() {
        User u = user(1L);

        ShoppingList list = new ShoppingList();
        list.setId(10L);
        list.setUser(u);
        list.setItems(List.of());

        when(shoppingListRepository.findById(10L))
                .thenReturn(Optional.of(list));

        service.finalizeList(u, 10L, true);

        verify(shoppingListRepository).delete(list);
    }
}
