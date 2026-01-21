package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.response.PantryItemResponse;
import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.model.PantryItem;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.example.cookingappbackend.repository.PantryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryServiceTest {

    private final PantryItemRepository pantryRepo = mock(PantryItemRepository.class);
    private final IngredientRepository ingredientRepo = mock(IngredientRepository.class);

    private final PantryService service = new PantryService(pantryRepo, ingredientRepo);

    @Test
    void list_mapsItemsToResponses() throws Exception {
        User user = new User();
        user.setId(1L);

        Ingredient ing1 = ingredientWithCategory(10L, "Milk", "ml");
        Ingredient ing2 = ingredientWithCategory(11L, "Rice", "g");

        PantryItem p1 = new PantryItem();
        p1.setId(100L);
        p1.setUser(user);
        p1.setIngredient(ing1);
        p1.setAmount(2.0);

        PantryItem p2 = new PantryItem();
        p2.setId(101L);
        p2.setUser(user);
        p2.setIngredient(ing2);
        p2.setAmount(500.0);

        when(pantryRepo.findByUserId(1L)).thenReturn(List.of(p1, p2));

        List<PantryItemResponse> result = service.list(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(100L);
        assertThat(result.get(0).ingredientId()).isEqualTo(10L);
        assertThat(result.get(0).ingredientName()).isEqualTo("Milk");
        assertThat(result.get(0).ingredientUnit()).isEqualTo("ml");
        assertThat(result.get(0).amount()).isEqualTo(2.0);

        assertThat(result.get(1).id()).isEqualTo(101L);
        assertThat(result.get(1).ingredientId()).isEqualTo(11L);
        assertThat(result.get(1).ingredientName()).isEqualTo("Rice");
        assertThat(result.get(1).ingredientUnit()).isEqualTo("g");
        assertThat(result.get(1).amount()).isEqualTo(500.0);

        verify(pantryRepo).findByUserId(1L);
        verifyNoMoreInteractions(pantryRepo);
        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void upsert_whenItemExists_updatesAmount_andSaves() throws Exception {
        User user = new User();
        user.setId(1L);

        Ingredient ing = ingredientWithCategory(10L, "Milk", "ml");

        PantryItem existing = new PantryItem();
        existing.setId(100L);
        existing.setUser(user);
        existing.setIngredient(ing);
        existing.setAmount(1.0);

        when(pantryRepo.findByUserIdAndIngredientId(1L, 10L)).thenReturn(Optional.of(existing));
        when(pantryRepo.save(any(PantryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        PantryItemResponse res = service.upsert(user, 10L, 3.5);

        assertThat(res.id()).isEqualTo(100L);
        assertThat(res.ingredientId()).isEqualTo(10L);
        assertThat(res.ingredientName()).isEqualTo("Milk");
        assertThat(res.ingredientUnit()).isEqualTo("ml");
        assertThat(res.amount()).isEqualTo(3.5);

        ArgumentCaptor<PantryItem> captor = ArgumentCaptor.forClass(PantryItem.class);
        verify(pantryRepo).findByUserIdAndIngredientId(1L, 10L);
        verify(pantryRepo).save(captor.capture());

        PantryItem saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(3.5);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getIngredient()).isSameAs(ing);

        verifyNoMoreInteractions(pantryRepo);
        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void upsert_whenItemMissing_createsNew_fetchesIngredient_setsAmount_saves() throws Exception {
        User user = new User();
        user.setId(1L);

        Ingredient ing = ingredientWithCategory(10L, "Milk", "ml");

        when(pantryRepo.findByUserIdAndIngredientId(1L, 10L)).thenReturn(Optional.empty());
        when(ingredientRepo.findById(10L)).thenReturn(Optional.of(ing));
        when(pantryRepo.save(any(PantryItem.class))).thenAnswer(inv -> {
            PantryItem pi = inv.getArgument(0);
            pi.setId(200L);
            return pi;
        });

        PantryItemResponse res = service.upsert(user, 10L, 2.0);

        assertThat(res.id()).isEqualTo(200L);
        assertThat(res.ingredientId()).isEqualTo(10L);
        assertThat(res.ingredientName()).isEqualTo("Milk");
        assertThat(res.ingredientUnit()).isEqualTo("ml");
        assertThat(res.amount()).isEqualTo(2.0);

        ArgumentCaptor<PantryItem> captor = ArgumentCaptor.forClass(PantryItem.class);
        verify(pantryRepo).findByUserIdAndIngredientId(1L, 10L);
        verify(ingredientRepo).findById(10L);
        verify(pantryRepo).save(captor.capture());

        PantryItem saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getIngredient()).isSameAs(ing);
        assertThat(saved.getAmount()).isEqualTo(2.0);

        verifyNoMoreInteractions(pantryRepo);
        verifyNoMoreInteractions(ingredientRepo);
    }

    @Test
    void upsert_whenIngredientNotFound_throws() {
        User user = new User();
        user.setId(1L);

        when(pantryRepo.findByUserIdAndIngredientId(1L, 10L)).thenReturn(Optional.empty());
        when(ingredientRepo.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsert(user, 10L, 1.0))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Ingredient not found: 10");

        verify(pantryRepo).findByUserIdAndIngredientId(1L, 10L);
        verify(ingredientRepo).findById(10L);
        verifyNoMoreInteractions(pantryRepo);
        verifyNoMoreInteractions(ingredientRepo);
    }

    @Test
    void delete_whenNotFound_throws() {
        User user = new User();
        user.setId(1L);

        when(pantryRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(user, 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Pantry item not found");

        verify(pantryRepo).findById(999L);
        verifyNoMoreInteractions(pantryRepo);
        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void delete_whenNotOwner_throwsSecurityException() throws Exception {
        User user = new User();
        user.setId(1L);

        User other = new User();
        other.setId(2L);

        Ingredient ing = ingredientWithCategory(10L, "Milk", "ml");

        PantryItem item = new PantryItem();
        item.setId(100L);
        item.setUser(other);
        item.setIngredient(ing);
        item.setAmount(1.0);

        when(pantryRepo.findById(100L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.delete(user, 100L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not your pantry item");

        verify(pantryRepo).findById(100L);
        verifyNoMoreInteractions(pantryRepo);
        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void delete_whenOwner_deletesItem() throws Exception {
        User user = new User();
        user.setId(1L);

        Ingredient ing = ingredientWithCategory(10L, "Milk", "ml");

        PantryItem item = new PantryItem();
        item.setId(100L);
        item.setUser(user);
        item.setIngredient(ing);
        item.setAmount(1.0);

        when(pantryRepo.findById(100L)).thenReturn(Optional.of(item));

        service.delete(user, 100L);

        verify(pantryRepo).findById(100L);
        verify(pantryRepo).delete(item);
        verifyNoMoreInteractions(pantryRepo);
        verifyNoInteractions(ingredientRepo);
    }

    private static Ingredient ingredientWithCategory(Long id, String name, String unit) throws Exception {
        Ingredient ing = new Ingredient();
        ing.setId(id);
        ing.setName(name);
        ing.setUnit(unit);

        Method m = Ingredient.class.getMethod("getCategory");
        Class<?> categoryType = m.getReturnType();
        Object[] constants = categoryType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return ing;
        }

        Object firstEnum = constants[0];

        try {
            Method setter = Ingredient.class.getMethod("setCategory", categoryType);
            setter.invoke(ing, firstEnum);
            return ing;
        } catch (NoSuchMethodException e) {
            return ing;
        }
    }
}
