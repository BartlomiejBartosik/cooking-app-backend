package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
     select distinct r from Recipe r
     left join fetch r.ingredients ri
     left join fetch ri.ingredient
  """)
    List<Recipe> findAllWithIngredients();

    @Query("""
     select distinct r from Recipe r
     left join fetch r.ingredients ri
     left join fetch ri.ingredient
     where r.id = :id
  """)
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);
}