package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientRepository ingredientRepo;

    record IngredientDto(Long id, String name, String unit, String category) {}

    @GetMapping
    public ResponseEntity<List<IngredientDto>> search(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        String q = query.trim().toLowerCase();

        List<Ingredient> ranked = ingredientRepo.searchRanked(q);
        if (ranked.size() > limit) ranked = ranked.subList(0, limit);

        List<IngredientDto> dto = ranked.stream()
                .map(i -> new IngredientDto(
                        i.getId(),
                        i.getName(),
                        i.getUnit(),
                        i.getCategory().name()
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }
}
