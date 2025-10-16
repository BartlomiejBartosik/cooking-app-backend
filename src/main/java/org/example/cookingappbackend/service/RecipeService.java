package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.RecipeCreateRequest;
import org.example.cookingappbackend.dto.response.RecipeResponse;
import org.example.cookingappbackend.dto.response.RecipeSearchResultResponse;
import org.example.cookingappbackend.model.*;
import org.example.cookingappbackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;

    @Transactional
    public RecipeResponse create(RecipeCreateRequest req, User author) {
        Recipe r = new Recipe();
        r.setTitle(req.getTitle());
        r.setDescription(req.getDescription());
        r.setTotalTimeMin(req.getTotalTimeMin());
        r.setAuthor(author);

        if (req.getIngredients() != null) {
            for (var it : req.getIngredients()) {
                Ingredient ing = ingredientRepo.findById(it.getIngredientId())
                        .orElseThrow(() -> new NoSuchElementException("Ingredient not found: " + it.getIngredientId()));
                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipe(r);
                ri.setIngredient(ing);
                ri.setAmount(it.getAmount());
                r.getIngredients().add(ri);
            }
        }

        if (req.getSteps() != null) {
            for (var st : req.getSteps()) {
                RecipeStep step = new RecipeStep();
                step.setRecipe(r);
                step.setStepNo(st.getStepNo());
                step.setInstruction(st.getInstruction());
                step.setTimeMin(st.getTimeMin());
                r.getSteps().add(step);
            }
            r.getSteps().sort(Comparator.comparing(RecipeStep::getStepNo));
        }

        r = recipeRepo.save(r);
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> list() {
        return recipeRepo.findAllWithIngredients()
                .stream()
                .map(this::toResponseWithoutSteps)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse get(Long id) {
        Recipe r = recipeRepo.findByIdWithIngredients(id)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));
        r.getSteps().size();
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public List<RecipeSearchResultResponse> searchByIngredients(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();

        Set<String> want = names.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        List<Recipe> recipes = recipeRepo.findAllWithIngredients();
        List<RecipeSearchResultResponse> results = new ArrayList<>();

        for (Recipe r : recipes) {
            Set<String> recipeIngs = r.getIngredients().stream()
                    .map(ri -> ri.getIngredient().getName().toLowerCase())
                    .collect(Collectors.toSet());
            int matched = 0;
            for (String w : want) if (recipeIngs.contains(w)) matched++;
            int missing = Math.max(0, recipeIngs.size() - matched);
            if (matched > 0) {
                results.add(new RecipeSearchResultResponse(r.getId(), r.getTitle(), matched, missing));
            }
        }

        results.sort(Comparator
                .comparingInt(RecipeSearchResultResponse::getMatchedCount).reversed()
                .thenComparingInt(RecipeSearchResultResponse::getMissingCount));
        return results;
    }

    private RecipeResponse toResponse(Recipe r) {
        RecipeResponse res = new RecipeResponse();
        res.setId(r.getId());
        res.setTitle(r.getTitle());
        res.setDescription(r.getDescription());
        res.setTotalTimeMin(r.getTotalTimeMin());
        res.setAvgRating(r.getAvgRating());

        List<RecipeResponse.IngredientLine> ingLines = new ArrayList<>();
        for (RecipeIngredient ri : r.getIngredients()) {
            RecipeResponse.IngredientLine line = new RecipeResponse.IngredientLine();
            line.setIngredientId(ri.getIngredient().getId());
            line.setIngredientName(ri.getIngredient().getName());
            line.setUnit(ri.getIngredient().getUnit());
            line.setAmount(ri.getAmount());
            ingLines.add(line);
        }
        res.setIngredients(ingLines);

        List<RecipeResponse.StepLine> stepLines = new ArrayList<>();
        for (RecipeStep s : r.getSteps()) {
            RecipeResponse.StepLine sl = new RecipeResponse.StepLine();
            sl.setStepNo(s.getStepNo());
            sl.setInstruction(s.getInstruction());
            sl.setTimeMin(s.getTimeMin());
            stepLines.add(sl);
        }
        res.setSteps(stepLines);

        return res;
    }

    private RecipeResponse toResponseWithoutSteps(Recipe r) {
        RecipeResponse res = new RecipeResponse();
        res.setId(r.getId());
        res.setTitle(r.getTitle());
        res.setDescription(r.getDescription());
        res.setTotalTimeMin(r.getTotalTimeMin());
        res.setAvgRating(r.getAvgRating());

        List<RecipeResponse.IngredientLine> ingLines = new ArrayList<>();
        for (RecipeIngredient ri : r.getIngredients()) {
            RecipeResponse.IngredientLine line = new RecipeResponse.IngredientLine();
            line.setIngredientId(ri.getIngredient().getId());
            line.setIngredientName(ri.getIngredient().getName());
            line.setUnit(ri.getIngredient().getUnit());
            line.setAmount(ri.getAmount());
            ingLines.add(line);
        }
        res.setIngredients(ingLines);


        return res;
    }
}

