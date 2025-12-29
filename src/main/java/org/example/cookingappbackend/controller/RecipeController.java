package org.example.cookingappbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.RecipeCreateRequest;
import org.example.cookingappbackend.dto.response.*;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.RecipeService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<RecipeSummaryResponse>> list(
            @PageableDefault(size = 20, sort = "title") Pageable pageable
    ) {
        return ResponseEntity.ok(recipeService.list(pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.get(id));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<RecipeSummaryResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(name = "query", required = false) String queryAlias,
            @RequestParam(required = false) String ingredients,
            @RequestParam(required = false, defaultValue = "false") boolean inPantryOnly,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if ((q == null || q.isBlank()) && queryAlias != null && !queryAlias.isBlank()) {
            q = queryAlias;
        }
        return ResponseEntity.ok(
                recipeService.search(q, ingredients, inPantryOnly, currentUser, pageable)
        );
    }
    @GetMapping("/top-rated")
    public ResponseEntity<Page<RecipeSummaryResponse>> listTopRated(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(recipeService.listTopRated(pageable));
    }
}
