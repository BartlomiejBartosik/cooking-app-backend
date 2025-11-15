package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.ShoppingList;
import org.example.cookingappbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    List<ShoppingList> findByUserOrderByCreatedAtDesc(User user);

    Optional<ShoppingList> findByIdAndUser(Long id, User user);

    boolean existsByUserAndNameIgnoreCase(User user, String name);
}
