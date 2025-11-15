package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.RatingRequest;
import org.example.cookingappbackend.dto.response.RatingResponse;
import org.example.cookingappbackend.model.Rating;
import org.example.cookingappbackend.model.Recipe;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.RatingRepository;
import org.example.cookingappbackend.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<RatingResponse> getRatings(Long recipeId, User currentUser) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        List<Rating> ratings = ratingRepository.findByRecipeIdOrderByCreatedAtDesc(recipe.getId());
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        return ratings.stream()
                .map(r -> toResponse(r, currentUserId))
                .toList();
    }

    @Transactional
    public void upsertRating(Long recipeId, User currentUser, RatingRequest req) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (req.getStars() == null || req.getStars() < 1 || req.getStars() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stars must be between 1 and 5");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        Rating rating = ratingRepository.findByRecipeAndUser(recipe, currentUser)
                .orElseGet(() -> {
                    Rating r = new Rating();
                    r.setRecipe(recipe);
                    r.setUser(currentUser);
                    r.setCreatedAt(LocalDateTime.now());
                    return r;
                });

        rating.setStars(req.getStars());
        rating.setComment(req.getComment());
        ratingRepository.save(rating);

        recalcAvg(recipeId, recipe);
    }

    @Transactional
    public void deleteMyRating(Long recipeId, User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        Rating rating = ratingRepository.findByRecipeAndUser(recipe, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating not found"));

        ratingRepository.delete(rating);

        recalcAvg(recipeId, recipe);
    }

    private void recalcAvg(Long recipeId, Recipe recipe) {
        List<Rating> all = ratingRepository.findByRecipeId(recipeId);

        double avg = all.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);

        recipe.setAvgRating(avg);
        recipeRepository.save(recipe);
    }

    private RatingResponse toResponse(Rating rating, Long currentUserId) {
        String displayName = "Użytkownik";
        if (rating.getUser() != null) {
            String n = rating.getUser().getName();
            String s = rating.getUser().getSurname();
            if (n != null || s != null) {
                displayName =
                        (n != null ? n : "") +
                                ((n != null && s != null) ? " " : "") +
                                (s != null ? s : "");
            } else if (rating.getUser().getEmail() != null) {
                displayName = rating.getUser().getEmail();
            }
        }

        boolean mine = currentUserId != null
                && rating.getUser() != null
                && currentUserId.equals(rating.getUser().getId());

        return new RatingResponse(
                rating.getId(),
                displayName,
                rating.getStars(),
                rating.getComment(),
                rating.getCreatedAt(),
                mine
        );
    }
}
