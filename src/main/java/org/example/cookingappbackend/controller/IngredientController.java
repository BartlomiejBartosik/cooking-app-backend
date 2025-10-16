package org.example.cookingappbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.response.IngredientResponse;
import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.service.IngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @PostMapping
    public ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientResponse dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> list() {
        return ResponseEntity.ok(service.list());
    }
}
