package org.example.cookingappbackend.repository;
import org.example.cookingappbackend.model.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {}
