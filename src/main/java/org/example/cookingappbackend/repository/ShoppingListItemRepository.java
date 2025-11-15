package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.ShoppingList;
import org.example.cookingappbackend.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    Optional<ShoppingListItem> findByIdAndShoppingList(Long id, ShoppingList shoppingList);
}
