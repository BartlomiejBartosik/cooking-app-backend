package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.model.PantryItem;
import org.example.cookingappbackend.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserId(Long userId);

    Optional<PantryItem> findByUserIdAndIngredientId(Long userId, Long ingredientId);

    @Query("""
        select lower(i.name) from PantryItem pi
        join pi.ingredient i
        where pi.user.id = :userId
    """)
    List<String> findIngredientNamesByUserId(@Param("userId") Long userId);

    List<PantryItem> findByUser(User user);

    Optional<PantryItem> findByUserAndIngredient(User user, Ingredient ingredient);

}
