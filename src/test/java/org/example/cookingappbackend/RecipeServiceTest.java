package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.request.RecipeCreateRequest;
import org.example.cookingappbackend.dto.response.RecipeResponse;
import org.example.cookingappbackend.dto.response.RecipeSearchResultResponse;
import org.example.cookingappbackend.dto.response.RecipeSummaryResponse;
import org.example.cookingappbackend.model.*;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.example.cookingappbackend.repository.PantryItemRepository;
import org.example.cookingappbackend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    private final RecipeRepository recipeRepo = mock(RecipeRepository.class);
    private final IngredientRepository ingredientRepo = mock(IngredientRepository.class);
    private final PantryItemRepository pantryRepo = mock(PantryItemRepository.class);

    private final RecipeService service = new RecipeService(recipeRepo, ingredientRepo, pantryRepo);

    @Test
    void create_savesRecipe_withIngredientsAndSteps_sortedByStepNo() throws Exception {
        RecipeCreateRequest req = new RecipeCreateRequest();
        req.setTitle("T");
        req.setDescription("D");
        req.setTotalTimeMin(30);

        Object item2 = newReqItem(2L, 2.0);
        Object item1 = newReqItem(1L, 1.5);
        setList(req, "setIngredients", List.class, List.of(item2, item1));

        Object step2 = newReqStep(2, "B", 10);
        Object step1 = newReqStep(1, "A", 5);
        setList(req, "setSteps", List.class, List.of(step2, step1));

        Ingredient ing1 = ingredient(1L, "I1", "g");
        Ingredient ing2 = ingredient(2L, "I2", "ml");
        when(ingredientRepo.findById(1L)).thenReturn(Optional.of(ing1));
        when(ingredientRepo.findById(2L)).thenReturn(Optional.of(ing2));

        when(recipeRepo.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(100L);
            r.setAvgRating(0.0);
            return r;
        });

        RecipeResponse res = service.create(req, new User());

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getTitle()).isEqualTo("T");
        assertThat(res.getDescription()).isEqualTo("D");
        assertThat(res.getTotalTimeMin()).isEqualTo(30);

        assertThat(res.getIngredients()).hasSize(2);
        assertThat(res.getIngredients().stream().map(RecipeResponse.IngredientLine::getIngredientId))
                .containsExactlyInAnyOrder(1L, 2L);

        assertThat(res.getSteps()).hasSize(2);
        assertThat(res.getSteps().get(0).getStepNo()).isEqualTo(1);
        assertThat(res.getSteps().get(0).getInstruction()).isEqualTo("A");
        assertThat(res.getSteps().get(1).getStepNo()).isEqualTo(2);
        assertThat(res.getSteps().get(1).getInstruction()).isEqualTo("B");

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepo).save(captor.capture());
        Recipe saved = captor.getValue();

        assertThat(saved.getSteps()).hasSize(2);
        assertThat(saved.getSteps().get(0).getStepNo()).isEqualTo(1);
        assertThat(saved.getSteps().get(1).getStepNo()).isEqualTo(2);

        verify(ingredientRepo).findById(2L);
        verify(ingredientRepo).findById(1L);
        verifyNoMoreInteractions(recipeRepo, ingredientRepo, pantryRepo);
    }

    @Test
    void create_whenIngredientMissing_throws() throws Exception {
        RecipeCreateRequest req = new RecipeCreateRequest();
        req.setTitle("T");
        req.setDescription("D");
        req.setTotalTimeMin(10);

        Object item = newReqItem(99L, 1.0);
        setList(req, "setIngredients", List.class, List.of(item));
        setList(req, "setSteps", List.class, List.of());

        when(ingredientRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req, new User()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Ingredient not found: 99");

        verify(ingredientRepo).findById(99L);
        verifyNoInteractions(recipeRepo, pantryRepo);
        verifyNoMoreInteractions(ingredientRepo);
    }

    @Test
    void searchByIngredients_returnsEmpty_whenNullOrEmpty() {
        assertThat(service.searchByIngredients(null)).isEmpty();
        assertThat(service.searchByIngredients(List.of())).isEmpty();
        verifyNoInteractions(recipeRepo, ingredientRepo, pantryRepo);
    }

    @Test
    void searchByIngredients_ranksByMatchedDesc_missingAsc() {
        Recipe r1 = recipeWithIngredientNames(1L, "A", List.of("salt", "pepper", "oil"));
        Recipe r2 = recipeWithIngredientNames(2L, "B", List.of("salt"));
        Recipe r3 = recipeWithIngredientNames(3L, "C", List.of("tomato", "basil"));

        when(recipeRepo.findAllWithIngredients()).thenReturn(List.of(r1, r2, r3));

        List<RecipeSearchResultResponse> res = service.searchByIngredients(List.of("SALT", "oil"));

        assertThat(res).isNotEmpty();
        assertThat(res.get(0).getId()).isEqualTo(1L);

        verify(recipeRepo).findAllWithIngredients();
        verifyNoMoreInteractions(recipeRepo);
        verifyNoInteractions(ingredientRepo, pantryRepo);
    }

    @Test
    void search_whenQProvided_usesTitleQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        Recipe r1 = new Recipe();
        r1.setId(1L);
        r1.setTitle("Tomato soup");

        when(recipeRepo.findByTitleContainingIgnoreCase("tomato", pageable))
                .thenReturn(new PageImpl<>(List.of(r1), pageable, 1));

        Page<RecipeSummaryResponse> page = service.search(" tomato ", null, false, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);

        verify(recipeRepo).findByTitleContainingIgnoreCase("tomato", pageable);
        verifyNoMoreInteractions(recipeRepo);
        verifyNoInteractions(ingredientRepo, pantryRepo);
    }

    @Test
    void search_whenIngredientsCsvProvided_usesIngredientSearch() {
        Pageable pageable = PageRequest.of(0, 20);

        Recipe r1 = new Recipe();
        r1.setId(1L);
        r1.setTitle("X");

        when(recipeRepo.searchByIngredientNames(List.of("a", "b"), 2, pageable))
                .thenReturn(new PageImpl<>(List.of(r1), pageable, 1));

        Page<RecipeSummaryResponse> page = service.search(null, " A, b ", false, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);

        verify(recipeRepo).searchByIngredientNames(List.of("a", "b"), 2, pageable);
        verifyNoMoreInteractions(recipeRepo);
        verifyNoInteractions(ingredientRepo, pantryRepo);
    }

    @Test
    void search_whenInPantryOnly_andNoUser_returnsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<RecipeSummaryResponse> page = service.search(null, null, true, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(0);
        verifyNoInteractions(recipeRepo, ingredientRepo, pantryRepo);
    }

    @Test
    void search_whenInPantryOnly_andEmptyPantry_returnsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);

        User u = new User();
        u.setId(1L);

        when(pantryRepo.findIngredientNamesByUserId(1L)).thenReturn(List.of());

        Page<RecipeSummaryResponse> page = service.search(null, null, true, u, pageable);

        assertThat(page.getTotalElements()).isEqualTo(0);

        verify(pantryRepo).findIngredientNamesByUserId(1L);
        verifyNoMoreInteractions(pantryRepo);
        verifyNoInteractions(recipeRepo, ingredientRepo);
    }

    @Test
    void search_whenInPantryOnly_usesPantryRanked() {
        Pageable pageable = PageRequest.of(0, 20);

        User u = new User();
        u.setId(1L);

        when(pantryRepo.findIngredientNamesByUserId(1L)).thenReturn(List.of("salt", "oil"));

        Recipe r1 = new Recipe();
        r1.setId(1L);
        r1.setTitle("X");

        when(recipeRepo.searchPantryRanked(List.of("salt", "oil"), 1L, pageable))
                .thenReturn(new PageImpl<>(List.of(r1), pageable, 1));

        Page<RecipeSummaryResponse> page = service.search(null, null, true, u, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);

        verify(pantryRepo).findIngredientNamesByUserId(1L);
        verify(recipeRepo).searchPantryRanked(List.of("salt", "oil"), 1L, pageable);
        verifyNoMoreInteractions(pantryRepo, recipeRepo);
        verifyNoInteractions(ingredientRepo);
    }

    @Test
    void listTopRated_delegatesToRepo() {
        Pageable pageable = PageRequest.of(0, 10);

        Recipe r1 = new Recipe();
        r1.setId(1L);
        r1.setTitle("X");
        r1.setAvgRating(5.0);

        when(recipeRepo.findAllByOrderByAvgRatingDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(r1), pageable, 1));

        Page<RecipeSummaryResponse> page = service.listTopRated(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);

        verify(recipeRepo).findAllByOrderByAvgRatingDesc(pageable);
        verifyNoMoreInteractions(recipeRepo);
        verifyNoInteractions(ingredientRepo, pantryRepo);
    }

    private static Object newReqItem(Long ingredientId, Double amount) throws Exception {
        Class<?> itemClass = Class.forName("org.example.cookingappbackend.dto.request.RecipeCreateRequest$Item");
        Object item = newInstance(itemClass);
        invokeIfExists(item, "setIngredientId", new Class[]{Long.class}, new Object[]{ingredientId});
        invokeIfExists(item, "setAmount", new Class[]{Double.class}, new Object[]{amount});
        return item;
    }

    private static Object newReqStep(Integer stepNo, String instruction, Integer timeMin) throws Exception {
        Class<?> stepClass = Class.forName("org.example.cookingappbackend.dto.request.RecipeCreateRequest$Step");
        Object step = newInstance(stepClass);
        invokeIfExists(step, "setStepNo", new Class[]{Integer.class}, new Object[]{stepNo});
        invokeIfExists(step, "setStepNo", new Class[]{int.class}, new Object[]{stepNo});
        invokeIfExists(step, "setInstruction", new Class[]{String.class}, new Object[]{instruction});
        invokeIfExists(step, "setTimeMin", new Class[]{Integer.class}, new Object[]{timeMin});
        invokeIfExists(step, "setTimeMin", new Class[]{int.class}, new Object[]{timeMin});
        return step;
    }

    private static void setList(Object target, String methodName, Class<?> paramType, List<?> value) throws Exception {
        Method m = null;
        for (Method mm : target.getClass().getMethods()) {
            if (mm.getName().equals(methodName) && mm.getParameterCount() == 1) {
                m = mm;
                break;
            }
        }
        if (m == null) throw new IllegalStateException("Brak metody " + methodName + " w " + target.getClass());
        m.invoke(target, value);
    }

    private static Object newInstance(Class<?> type) throws Exception {
        try {
            Constructor<?> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            Constructor<?> c = type.getDeclaredConstructors()[0];
            c.setAccessible(true);
            Object[] args = new Object[c.getParameterCount()];
            Arrays.fill(args, null);
            return c.newInstance(args);
        }
    }

    private static void invokeIfExists(Object target, String name, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(name, paramTypes);
            m.invoke(target, args);
        } catch (Exception ignored) {
        }
    }

    private static Ingredient ingredient(Long id, String name, String unit) {
        Ingredient ing = new Ingredient();
        ing.setId(id);
        ing.setName(name);
        ing.setUnit(unit);
        setAnyCategoryIfPossible(ing);
        return ing;
    }

    private static void setAnyCategoryIfPossible(Ingredient ing) {
        try {
            Method getter = Ingredient.class.getMethod("getCategory");
            Class<?> categoryType = getter.getReturnType();
            Object[] constants = categoryType.getEnumConstants();
            if (constants == null || constants.length == 0) return;

            try {
                Method setter = Ingredient.class.getMethod("setCategory", categoryType);
                setter.invoke(ing, constants[0]);
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    private static Recipe recipeWithIngredientNames(Long id, String title, List<String> namesLower) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setTitle(title);

        for (String n : namesLower) {
            Ingredient ing = ingredient(null, n, "u");
            RecipeIngredient ri = new RecipeIngredient();
            ri.setRecipe(r);
            ri.setIngredient(ing);
            ri.setAmount(1.0);
            r.getIngredients().add(ri);
        }
        return r;
    }
}
