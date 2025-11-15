package org.example.cookingappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.AddFromRecipeRequest;
import org.example.cookingappbackend.dto.request.ShoppingListItemRequest;
import org.example.cookingappbackend.dto.response.ShoppingListDetailsResponse;
import org.example.cookingappbackend.dto.response.ShoppingListSummaryResponse;
import org.example.cookingappbackend.model.User;
import org.example.cookingappbackend.service.ShoppingListService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public List<ShoppingListSummaryResponse> list(@AuthenticationPrincipal User currentUser) {
        return shoppingListService.list(currentUser);
    }

    @PostMapping
    public ShoppingListDetailsResponse create(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String name
    ) {
        return shoppingListService.create(currentUser, name);
    }

    @GetMapping("/{listId}")
    public ShoppingListDetailsResponse get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long listId
    ) {
        return shoppingListService.get(currentUser, listId);
    }

    @PostMapping("/{listId}/items")
    public ShoppingListDetailsResponse addItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long listId,
            @RequestBody ShoppingListItemRequest req
    ) {
        return shoppingListService.addItem(currentUser, listId, req);
    }

    @PutMapping("/{listId}/items/{itemId}")
    public ShoppingListDetailsResponse updateItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long listId,
            @PathVariable Long itemId,
            @RequestBody ShoppingListItemRequest req
    ) {
        return shoppingListService.updateItem(currentUser, listId, itemId, req);
    }

    @DeleteMapping("/{listId}/items/{itemId}")
    public ShoppingListDetailsResponse deleteItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long listId,
            @PathVariable Long itemId
    ) {
        return shoppingListService.deleteItem(currentUser, listId, itemId);
    }

    @PostMapping("/{id}/finalize")
    public void finalizeList(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean addToPantry
    ) {
        shoppingListService.finalizeList(currentUser, id, addToPantry);
    }

    @PostMapping("/add-from-recipe/{recipeId}")
    public ShoppingListDetailsResponse addFromRecipe(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long recipeId,
            @RequestBody AddFromRecipeRequest req
    ) {
        return shoppingListService.addFromRecipe(currentUser, recipeId, req);
    }
    @PatchMapping("/{id}")
    public ShoppingListDetailsResponse rename(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return shoppingListService.rename(currentUser, id, body.get("name"));
    }
}
