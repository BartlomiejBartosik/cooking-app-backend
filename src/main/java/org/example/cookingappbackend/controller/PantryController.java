package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.PantryItemUpsertRequest;
import org.example.cookingappbackend.dto.response.PantryItemResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.PantryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
@RequiredArgsConstructor
public class PantryController {

    private final PantryService pantryService;

    @GetMapping
    public List<PantryItemResponse> list(@AuthenticationPrincipal User currentUser) {
        return pantryService.list(currentUser);
    }

    @PostMapping
    public PantryItemResponse upsert(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PantryItemUpsertRequest req
    ) {
        return pantryService.upsert(currentUser, req.getIngredientId(), req.getAmount());
    }

    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        pantryService.delete(currentUser, id);
    }
}
