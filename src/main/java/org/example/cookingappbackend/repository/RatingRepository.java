package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.Rating;
import org.example.cookingappbackend.model.Recipe;
import org.example.cookingappbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);

    List<Rating> findByRecipeId(Long recipeId);

    Optional<Rating> findByRecipeAndUser(Recipe recipe, User user);
}
