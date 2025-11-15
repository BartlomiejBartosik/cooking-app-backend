package org.example.cookingappbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.cookingappbackend.dto.request.AddFromRecipeRequest;
import org.example.cookingappbackend.dto.request.ShoppingListItemRequest;
import org.example.cookingappbackend.dto.response.ShoppingListDetailsResponse;
import org.example.cookingappbackend.dto.response.ShoppingListItemResponse;
import org.example.cookingappbackend.dto.response.ShoppingListSummaryResponse;
import org.example.cookingappbackend.repository.ShoppingListRepository;
import org.example.cookingappbackend.model.*;
import org.example.cookingappbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository listRepo;
    private final ShoppingListItemRepository itemRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeRepository recipeRepo;
    private final PantryItemRepository pantryItemRepository;
    private final PantryService pantryService;
    private final ShoppingListRepository shoppingListRepository;


    @Transactional(readOnly = true)
    public List<ShoppingListSummaryResponse> list(User user) {
        ensureUser(user);

        return listRepo.findByUserOrderByCreatedAtDesc(user).stream()
                .map(list -> {
                    ShoppingListSummaryResponse dto = new ShoppingListSummaryResponse();
                    dto.setId(list.getId());
                    dto.setName(list.getName());
                    dto.setItemsCount(list.getItems() != null ? list.getItems().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShoppingListDetailsResponse get(User user, Long listId) {
        ShoppingList list = getOwnedList(user, listId);
        return toDetails(list);
    }

    @Transactional
    public ShoppingListDetailsResponse create(User user, String name) {
        ensureUser(user);

        String baseName = (name == null || name.trim().isEmpty())
                ? "Lista zakupów"
                : name.trim();

        String finalName = baseName;
        int i = 2;
        while (listRepo.existsByUserAndNameIgnoreCase(user, finalName)) {
            finalName = baseName + " (" + i + ")";
            i++;
        }

        ShoppingList list = new ShoppingList();
        list.setUser(user);
        list.setName(finalName);
        list.setCreatedAt(LocalDateTime.now());
        listRepo.save(list);

        return toDetails(list);
    }


    @Transactional
    public ShoppingListDetailsResponse addItem(User user, Long listId, ShoppingListItemRequest req) {
        ShoppingList list = getOwnedList(user, listId);

        Ingredient ingredient = null;
        String name;

        if (req.getIngredientId() != null) {
            ingredient = ingredientRepo.findById(req.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Składnik nie istnieje"));
            name = ingredient.getName();
        } else {
            if (req.getName() == null || req.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nazwa jest wymagana");
            }
            name = req.getName().trim();
        }

        ShoppingListItem item = new ShoppingListItem();
        item.setShoppingList(list);
        item.setIngredient(ingredient);
        item.setName(name);
        item.setAmount(req.getAmount());
        item.setUnit(req.getUnit());
        itemRepo.save(item);

        list.getItems().add(item);

        return toDetails(list);
    }

    @Transactional
    public ShoppingListDetailsResponse updateItem(User user, Long listId, Long itemId, ShoppingListItemRequest req) {
        ShoppingList list = getOwnedList(user, listId);

        ShoppingListItem item = itemRepo.findByIdAndShoppingList(itemId, list)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pozycja nie istnieje"));

        if (req.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepo.findById(req.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Składnik nie istnieje"));
            item.setIngredient(ingredient);
            item.setName(ingredient.getName());
            if (req.getUnit() != null) {
                item.setUnit(req.getUnit());
            } else if (ingredient.getUnit() != null) {
                item.setUnit(ingredient.getUnit());
            }
        } else if (req.getName() != null && !req.getName().trim().isEmpty()) {
            item.setIngredient(null);
            item.setName(req.getName().trim());
            if (req.getUnit() != null) {
                item.setUnit(req.getUnit());
            }
        } else {
            if (req.getUnit() != null) {
                item.setUnit(req.getUnit());
            }
        }

        if (req.getAmount() != null) {
            item.setAmount(req.getAmount());
        }

        itemRepo.save(item);
        return toDetails(list);
    }

    @Transactional
    public ShoppingListDetailsResponse deleteItem(User user, Long listId, Long itemId) {
        ShoppingList list = getOwnedList(user, listId);

        ShoppingListItem item = itemRepo.findByIdAndShoppingList(itemId, list)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pozycja nie istnieje"));

        itemRepo.delete(item);
        if (list.getItems() != null) {
            list.getItems().removeIf(i -> Objects.equals(i.getId(), itemId));
        }

        return toDetails(list);
    }


    @Transactional
    public void finalizeList(User user, Long listId, boolean addToPantry) {
        ShoppingList list = shoppingListRepository
                .findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Lista nie istnieje"));

        if (!list.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Brak dostępu do tej listy");
        }

        if (addToPantry) {
            list.getItems().forEach(item -> mergeItemIntoPantry(user, item));
        }

        shoppingListRepository.delete(list);
    }


    @Transactional
    public ShoppingListDetailsResponse addFromRecipe(User user, Long recipeId, AddFromRecipeRequest req) {
        ensureUser(user);

        Recipe recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Przepis nie istnieje"));

        String mode = (req.getMode() == null)
                ? "missing"
                : req.getMode().toLowerCase(Locale.ROOT);

        ShoppingList target = resolveTargetList(user, recipe, req);

        List<PantryItem> pantryItems = pantryItemRepository.findByUser(user);
        Set<Long> pantryIngredientIds = pantryItems.stream()
                .map(pi -> pi.getIngredient() != null ? pi.getIngredient().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> existingIngIds = target.getItems().stream()
                .map(i -> i.getIngredient() != null ? i.getIngredient().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingNames = target.getItems().stream()
                .filter(i -> i.getIngredient() == null && i.getName() != null)
                .map(i -> i.getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        if (recipe.getIngredients() != null) {
            for (RecipeIngredient ri : recipe.getIngredients()) {
                Ingredient ing = ri.getIngredient();
                if (ing == null || ing.getName() == null || ing.getName().trim().isEmpty()) {
                    continue;
                }

                String name = ing.getName().trim();
                boolean hasInPantry = pantryIngredientIds.contains(ing.getId());

                if ("missing".equals(mode) && hasInPantry) {
                    continue;
                }

                if (existingIngIds.contains(ing.getId())) continue;
                if (existingNames.contains(name.toLowerCase())) continue;

                ShoppingListItem item = new ShoppingListItem();
                item.setShoppingList(target);
                item.setIngredient(ing);
                item.setName(name);
                item.setAmount(ri.getAmount());
                try {
                    String unit = ing.getUnit();
                    item.setUnit(unit);
                } catch (NoSuchMethodError | Exception ex) {
                    item.setUnit(null);
                }

                itemRepo.save(item);
                target.getItems().add(item);
            }
        }

        return toDetails(target);
    }


    private void ensureUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    private ShoppingList getOwnedList(User user, Long id) {
        ensureUser(user);
        return listRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista nie istnieje"));
    }

    private ShoppingListDetailsResponse toDetails(ShoppingList list) {
        ShoppingListDetailsResponse dto = new ShoppingListDetailsResponse();
        dto.setId(list.getId());
        dto.setName(list.getName());

        List<ShoppingListItemResponse> items = new ArrayList<>();
        if (list.getItems() != null) {
            for (ShoppingListItem i : list.getItems()) {
                ShoppingListItemResponse ir = new ShoppingListItemResponse();
                ir.setId(i.getId());
                ir.setIngredientId(i.getIngredient() != null ? i.getIngredient().getId() : null);
                ir.setName(i.getName());
                ir.setAmount(i.getAmount());
                ir.setUnit(i.getUnit());
                items.add(ir);
            }
        }
        dto.setItems(items);

        return dto;
    }

    private ShoppingList resolveTargetList(User user, Recipe recipe, AddFromRecipeRequest req) {
        if (req.getListId() != null) {
            return getOwnedList(user, req.getListId());
        }

        String baseName;
        if (req.getNewListName() != null && !req.getNewListName().trim().isEmpty()) {
            baseName = req.getNewListName().trim();
        } else {
            baseName = "Lista: " + recipe.getTitle();
        }

        String name = baseName;
        int i = 2;
        while (listRepo.existsByUserAndNameIgnoreCase(user, name)) {
            name = baseName + " (" + i + ")";
            i++;
        }

        ShoppingList list = new ShoppingList();
        list.setUser(user);
        list.setName(name);
        list.setCreatedAt(LocalDateTime.now());
        listRepo.save(list);

        return list;
    }
    private void mergeItemIntoPantry(User user, ShoppingListItem item) {
        Double amount = item.getAmount();
        if (amount == null || amount <= 0d) {
            return;
        }

        Ingredient ingredient = item.getIngredient();
        if (ingredient == null) {
            return;
        }

        PantryItem pantryItem = pantryItemRepository
                .findByUserAndIngredient(user, ingredient)
                .orElseGet(() -> {
                    PantryItem pi = new PantryItem();
                    pi.setUser(user);
                    pi.setIngredient(ingredient);
                    pi.setAmount(0d);
                    return pi;
                });

        double current = pantryItem.getAmount() != null ? pantryItem.getAmount() : 0d;
        pantryItem.setAmount(current + amount);

        pantryItemRepository.save(pantryItem);
    }
    @Transactional
    public ShoppingListDetailsResponse rename(User user, Long listId, String newName) {
        ensureUser(user);
        if (newName == null || newName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nazwa jest wymagana");
        }

        ShoppingList list = getOwnedList(user, listId);
        list.setName(newName.trim());
        listRepo.save(list);

        return toDetails(list);
    }
}
