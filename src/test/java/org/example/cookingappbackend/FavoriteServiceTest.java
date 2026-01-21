package org.example.cookingappbackend.service;

import org.example.cookingappbackend.model.Favorite;
import org.example.cookingappbackend.model.Recipe;
import org.example.cookingappbackend.repository.FavoriteRepository;
import org.example.cookingappbackend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    private final FavoriteRepository favoriteRepo = mock(FavoriteRepository.class);
    private final RecipeRepository recipeRepo = mock(RecipeRepository.class);

    private final FavoriteService favoriteService = new FavoriteService(favoriteRepo, recipeRepo);

    @Test
    void add_whenAlreadyExists_doesNotSave_orFetchRecipe() {
        Long userId = 1L;
        Long recipeId = 2L;

        when(favoriteRepo.existsByUserIdAndRecipe_Id(userId, recipeId)).thenReturn(true);

        boolean result = favoriteService.add(userId, recipeId);

        assertThat(result).isTrue();
        verify(favoriteRepo).existsByUserIdAndRecipe_Id(userId, recipeId);
        verifyNoMoreInteractions(favoriteRepo);
        verifyNoInteractions(recipeRepo);
    }

    @Test
    void add_whenNotExists_fetchesRecipe_andSavesFavorite() {
        Long userId = 1L;
        Long recipeId = 2L;

        when(favoriteRepo.existsByUserIdAndRecipe_Id(userId, recipeId)).thenReturn(false);

        Recipe r = new Recipe();
        r.setId(recipeId);
        when(recipeRepo.findById(recipeId)).thenReturn(Optional.of(r));

        boolean result = favoriteService.add(userId, recipeId);

        assertThat(result).isTrue();

        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepo).existsByUserIdAndRecipe_Id(userId, recipeId);
        verify(recipeRepo).findById(recipeId);
        verify(favoriteRepo).save(captor.capture());

        Favorite saved = captor.getValue();
        assertThat(saved).isNotNull();

        verifyNoMoreInteractions(favoriteRepo, recipeRepo);
    }

    @Test
    void remove_deletesByUserIdAndRecipeId() {
        Long userId = 3L;
        Long recipeId = 4L;

        favoriteService.remove(userId, recipeId);

        verify(favoriteRepo).deleteByUserIdAndRecipeId(userId, recipeId);
        verifyNoMoreInteractions(favoriteRepo);
        verifyNoInteractions(recipeRepo);
    }

    @Test
    void list_mapsRecipesToSummaryResponse() {
        Long userId = 5L;
        var pageable = PageRequest.of(0, 20);

        Recipe r1 = new Recipe();
        r1.setId(1L);
        Recipe r2 = new Recipe();
        r2.setId(2L);

        Page<Recipe> recipes = new PageImpl<>(List.of(r1, r2), pageable, 2);
        when(favoriteRepo.findRecipesByUserId(userId, pageable)).thenReturn(recipes);

        var result = favoriteService.list(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        verify(favoriteRepo).findRecipesByUserId(userId, pageable);
        verifyNoMoreInteractions(favoriteRepo);
        verifyNoInteractions(recipeRepo);
    }

    @Test
    void ids_returnsRecipeIdsFromRepo() {
        Long userId = 6L;

        when(favoriteRepo.findRecipeIdsByUserId(userId)).thenReturn(List.of(10L, 20L, 30L));

        var ids = favoriteService.ids(userId);

        assertThat(ids).containsExactly(10L, 20L, 30L);

        verify(favoriteRepo).findRecipeIdsByUserId(userId);
        verifyNoMoreInteractions(favoriteRepo);
        verifyNoInteractions(recipeRepo);
    }
}
