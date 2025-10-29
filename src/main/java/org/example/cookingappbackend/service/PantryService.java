package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.response.PantryItemResponse;
import org.example.cookingappbackend.dto.request.PantryItemUpsertRequest;
import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.model.PantryItem;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.PantryItemRepository;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PantryService {

    private final PantryItemRepository pantryRepo;
    private final IngredientRepository ingredientRepo;

    @Transactional(readOnly = true)
    public List<PantryItemResponse> list(User currentUser) {
        return pantryRepo.findByUserId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PantryItemResponse upsert(User currentUser, Long ingredientId, Double amount) {
        PantryItem item = pantryRepo
                .findByUserIdAndIngredientId(currentUser.getId(), ingredientId)
                .orElseGet(() -> {
                    PantryItem pi = new PantryItem();
                    pi.setUser(currentUser);

                    Ingredient ing = ingredientRepo.findById(ingredientId)
                            .orElseThrow(() -> new NoSuchElementException("Ingredient not found: " + ingredientId));
                    pi.setIngredient(ing);

                    return pi;
                });

        item.setAmount(amount);

        PantryItem saved = pantryRepo.save(item);

        return toResponse(saved);
    }

    @Transactional
    public void delete(User currentUser, Long pantryItemId) {
        PantryItem item = pantryRepo.findById(pantryItemId)
                .orElseThrow(() -> new NoSuchElementException("Pantry item not found"));

        if (!item.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Not your pantry item");
        }

        pantryRepo.delete(item);
    }

    private PantryItemResponse toResponse(PantryItem pi) {
        return new PantryItemResponse(
                pi.getId(),
                pi.getIngredient().getId(),
                pi.getIngredient().getName(),
                pi.getIngredient().getCategory() != null
                        ? pi.getIngredient().getCategory().name()
                        : "OTHER",
                pi.getIngredient().getUnit(),
                pi.getAmount()
        );
    }
}
