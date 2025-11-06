package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.Favorite;
import org.example.cookingappbackend.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndRecipe_Id(Long userId, Long recipeId);

    @Modifying
    @Query("delete from Favorite f where f.userId = :userId and f.recipe.id = :recipeId")
    void deleteByUserIdAndRecipeId(@Param("userId") Long userId, @Param("recipeId") Long recipeId);

    @Query("select f.recipe from Favorite f where f.userId = :userId order by f.createdAt desc")
    Page<Recipe> findRecipesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select f.recipe.id from Favorite f where f.userId = :userId")
    List<Long> findRecipeIdsByUserId(@Param("userId") Long userId);
}
