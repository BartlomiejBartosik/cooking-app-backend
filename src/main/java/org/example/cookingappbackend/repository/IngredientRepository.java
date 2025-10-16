package org.example.cookingappbackend.repository;
import org.example.cookingappbackend.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {}
