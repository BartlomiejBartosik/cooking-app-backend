package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.RatingRequest;
import org.example.cookingappbackend.dto.response.RatingResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes/{recipeId}/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public List<RatingResponse> getRatings(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ratingService.getRatings(recipeId, currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertRating(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody RatingRequest req
    ) {
        ratingService.upsertRating(recipeId, currentUser, req);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyRating(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser
    ) {
        ratingService.deleteMyRating(recipeId, currentUser);
    }
}
