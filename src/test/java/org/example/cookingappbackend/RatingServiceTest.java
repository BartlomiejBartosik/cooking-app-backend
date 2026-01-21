package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.request.RatingRequest;
import org.example.cookingappbackend.dto.response.RatingResponse;
import org.example.cookingappbackend.model.Rating;
import org.example.cookingappbackend.model.Recipe;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.repository.RatingRepository;
import org.example.cookingappbackend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    private final RatingRepository ratingRepository = mock(RatingRepository.class);
    private final RecipeRepository recipeRepository = mock(RecipeRepository.class);

    private final RatingService service = new RatingService(ratingRepository, recipeRepository);

    @Test
    void getRatings_whenRecipeNotFound_throws404() {
        when(recipeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRatings(10L, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(recipeRepository).findById(10L);
        verifyNoMoreInteractions(recipeRepository);
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void getRatings_mapsResponses_andMarksMine() {
        Recipe recipe = new Recipe();
        recipe.setId(10L);
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));

        User me = new User();
        me.setId(1L);
        me.setEmail("me@example.com");

        User other = new User();
        other.setId(2L);
        other.setEmail("other@example.com");

        Rating r1 = new Rating();
        r1.setId(100L);
        r1.setRecipe(recipe);
        r1.setUser(me);
        r1.setStars(5);
        r1.setComment("Great");
        r1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Rating r2 = new Rating();
        r2.setId(101L);
        r2.setRecipe(recipe);
        r2.setUser(other);
        r2.setStars(3);
        r2.setComment("Ok");
        r2.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRecipeIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(r2, r1));

        List<RatingResponse> res = service.getRatings(10L, me);

        assertThat(res).hasSize(2);

        assertThat(getLong(res.get(0), "getId", "id")).isEqualTo(101L);
        assertThat(getBool(res.get(0), "isMine", "getMine", "mine")).isFalse();

        assertThat(getLong(res.get(1), "getId", "id")).isEqualTo(100L);
        assertThat(getBool(res.get(1), "isMine", "getMine", "mine")).isTrue();

        verify(recipeRepository).findById(10L);
        verify(ratingRepository).findByRecipeIdOrderByCreatedAtDesc(10L);
        verifyNoMoreInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void upsertRating_whenUnauthorized_throws401() {
        RatingRequest req = mock(RatingRequest.class);

        assertThatThrownBy(() -> service.upsertRating(10L, null, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        verifyNoInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void upsertRating_whenStarsInvalid_throws400() {
        User me = new User();
        me.setId(1L);

        RatingRequest req = mock(RatingRequest.class);
        when(req.getStars()).thenReturn(0);

        assertThatThrownBy(() -> service.upsertRating(10L, me, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void upsertRating_whenRecipeNotFound_throws404() {
        User me = new User();
        me.setId(1L);

        RatingRequest req = mock(RatingRequest.class);
        when(req.getStars()).thenReturn(5);

        when(recipeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertRating(10L, me, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(recipeRepository).findById(10L);
        verifyNoMoreInteractions(recipeRepository);
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void upsertRating_whenNewRating_createsAndSaves_andRecalculatesAvg() {
        long recipeId = 10L;
        User me = new User();
        me.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        RatingRequest req = mock(RatingRequest.class);
        when(req.getStars()).thenReturn(4);
        when(req.getComment()).thenReturn("Nice");

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.findByRecipeAndUser(recipe, me)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        Rating a = new Rating();
        a.setStars(4);
        Rating b = new Rating();
        b.setStars(2);
        when(ratingRepository.findByRecipeId(recipeId)).thenReturn(List.of(a, b));

        service.upsertRating(recipeId, me, req);

        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(recipeRepository).findById(recipeId);
        verify(ratingRepository).findByRecipeAndUser(recipe, me);
        verify(ratingRepository).save(ratingCaptor.capture());
        verify(ratingRepository).findByRecipeId(recipeId);
        verify(recipeRepository).save(recipe);

        Rating saved = ratingCaptor.getValue();
        assertThat(saved.getRecipe()).isSameAs(recipe);
        assertThat(saved.getUser()).isSameAs(me);
        assertThat(saved.getStars()).isEqualTo(4);
        assertThat(saved.getComment()).isEqualTo("Nice");
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(recipe.getAvgRating()).isEqualTo(3.0);
        verifyNoMoreInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void deleteMyRating_whenUnauthorized_throws401() {
        assertThatThrownBy(() -> service.deleteMyRating(10L, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verifyNoInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void deleteMyRating_whenRatingNotFound_throws404() {
        long recipeId = 10L;

        User me = new User();
        me.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.findByRecipeAndUser(recipe, me)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMyRating(recipeId, me))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(recipeRepository).findById(recipeId);
        verify(ratingRepository).findByRecipeAndUser(recipe, me);
        verifyNoMoreInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void deleteMyRating_whenExists_deletes_andRecalculatesAvg() {
        long recipeId = 10L;

        User me = new User();
        me.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        Rating rating = new Rating();
        rating.setId(100L);
        rating.setRecipe(recipe);
        rating.setUser(me);
        rating.setStars(4);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.findByRecipeAndUser(recipe, me)).thenReturn(Optional.of(rating));

        Rating a = new Rating();
        a.setStars(4);
        Rating b = new Rating();
        b.setStars(5);
        when(ratingRepository.findByRecipeId(recipeId)).thenReturn(List.of(a, b));

        service.deleteMyRating(recipeId, me);

        verify(ratingRepository).delete(rating);
        verify(ratingRepository).findByRecipeId(recipeId);
        verify(recipeRepository).save(recipe);

        assertThat(recipe.getAvgRating()).isEqualTo(4.5);

        verify(recipeRepository).findById(recipeId);
        verify(ratingRepository).findByRecipeAndUser(recipe, me);

        verifyNoMoreInteractions(recipeRepository, ratingRepository);
    }

    @Test
    void getRatings_displayName_prefersNameSurname() {
        Recipe recipe = new Recipe();
        recipe.setId(10L);
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));

        User u1 = new User();
        u1.setId(1L);
        u1.setEmail("x@example.com");
        u1.setName("Jan");
        u1.setSurname("Kowalski");

        Rating r1 = new Rating();
        r1.setId(1L);
        r1.setRecipe(recipe);
        r1.setUser(u1);
        r1.setStars(5);
        r1.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRecipeIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(r1));

        List<RatingResponse> res = service.getRatings(10L, null);

        assertThat(res).hasSize(1);
        assertThat(getString(
                res.get(0),
                "getUserName",
                "getUsername",
                "userName",
                "username"
        )).isEqualTo("Jan Kowalski");

        verify(recipeRepository).findById(10L);
        verify(ratingRepository).findByRecipeIdOrderByCreatedAtDesc(10L);
        verifyNoMoreInteractions(recipeRepository, ratingRepository);
    }

    private static Long getLong(Object obj, String... candidates) {
        Object v = getValue(obj, candidates);
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        throw new IllegalStateException("Nie mogę odczytać Long z: " + v);
    }

    private static boolean getBool(Object obj, String... candidates) {
        Object v = getValue(obj, candidates);
        if (v instanceof Boolean b) return b;
        throw new IllegalStateException("Nie mogę odczytać boolean z: " + v);
    }

    private static String getString(Object obj, String... candidates) {
        Object v = getValue(obj, candidates);
        return v != null ? v.toString() : null;
    }

    private static Object getValue(Object obj, String... candidates) {
        for (String name : candidates) {
            try {
                Method m = obj.getClass().getMethod(name);
                return m.invoke(obj);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException("Brak getterów: " + String.join(", ", candidates) + " w " + obj.getClass());
    }
}
