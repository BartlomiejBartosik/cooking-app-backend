package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.response.RecipeSummaryResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.FavoriteService;
import org.example.cookingappbackend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    @PostMapping("/recipes/{id}/favorite")
    public Map<String, Object> add(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        favoriteService.add(user.getId(), id);
        return Map.of("favorite", true);
    }

    @DeleteMapping("/recipes/{id}/favorite")
    public Map<String, Object> remove(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        favoriteService.remove(user.getId(), id);
        return Map.of("favorite", false);
    }

    @GetMapping("/favorites")
    public Page<RecipeSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        return favoriteService.list(user.getId(), PageRequest.of(page, size));
    }

    @GetMapping("/favorites/ids")
    public List<Long> ids(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        return favoriteService.ids(user.getId());
    }
}
