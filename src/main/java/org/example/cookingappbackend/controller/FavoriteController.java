package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.response.RecipeSummaryResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/recipes/{id}/favorite")
    public Map<String, Object> add(@PathVariable Long id,
                                   @AuthenticationPrincipal User currentUser) {
        favoriteService.add(currentUser.getId(), id);
        return Map.of("favorite", true);
    }

    @DeleteMapping("/recipes/{id}/favorite")
    public Map<String, Object> remove(@PathVariable Long id,
                                      @AuthenticationPrincipal User currentUser) {
        favoriteService.remove(currentUser.getId(), id);
        return Map.of("favorite", false);
    }

    @GetMapping("/favorites")
    public Page<RecipeSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @AuthenticationPrincipal User currentUser) {
        return favoriteService.list(currentUser.getId(), PageRequest.of(page, size));
    }

    @GetMapping("/favorites/ids")
    public List<Long> ids(@AuthenticationPrincipal User currentUser) {
        return favoriteService.ids(currentUser.getId());
    }
}
