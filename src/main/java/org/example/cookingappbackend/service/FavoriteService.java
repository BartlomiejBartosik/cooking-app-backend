package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.model.Recipe;
import org.example.cookingappbackend.repository.FavoriteRepository;
import org.example.cookingappbackend.repository.RecipeRepository;
import org.example.cookingappbackend.dto.response.RecipeSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepo;
    private final RecipeRepository recipeRepo;

    @Transactional
    public boolean add(Long userId, Long recipeId) {
        if (favoriteRepo.existsByUserIdAndRecipe_Id(userId, recipeId)) return true;
        Recipe r = recipeRepo.findById(recipeId).orElseThrow();
        favoriteRepo.save(new org.example.cookingappbackend.model.Favorite(userId, r));
        return true;
    }

    @Transactional
    public void remove(Long userId, Long recipeId) {
        favoriteRepo.deleteByUserIdAndRecipeId(userId, recipeId);
    }

    @Transactional(readOnly = true)
    public Page<RecipeSummaryResponse> list(Long userId, Pageable pageable) {
        return favoriteRepo.findRecipesByUserId(userId, pageable)
                .map(RecipeSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<Long> ids(Long userId) {
        return favoriteRepo.findRecipeIdsByUserId(userId);
    }
}
