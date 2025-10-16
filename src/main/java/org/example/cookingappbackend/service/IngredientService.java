package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.response.IngredientResponse;
import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository repo;

    public IngredientResponse create(IngredientResponse dto) {
        Ingredient ing = new Ingredient();
        ing.setName(dto.getName());
        ing.setUnit(dto.getUnit());
        try {
            ing = repo.save(ing);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ingredient with this name already exists");
        }
        dto.setId(ing.getId());
        return dto;
    }

    public List<Ingredient> list() {
        return repo.findAll();
    }
}
