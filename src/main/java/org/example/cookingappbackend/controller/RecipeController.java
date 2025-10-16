package org.example.cookingappbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.RecipeCreateRequest;
import org.example.cookingappbackend.dto.response.RecipeResponse;
import org.example.cookingappbackend.dto.response.RecipeSearchResultResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> create(@Valid @RequestBody RecipeCreateRequest req,
                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(recipeService.create(req, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> list() {
        return ResponseEntity.ok(recipeService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.get(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeSearchResultResponse>> search(@RequestParam String ingredients) {
        List<String> names = Arrays.stream(ingredients.split(",")).toList();
        return ResponseEntity.ok(recipeService.searchByIngredients(names));
    }
}

