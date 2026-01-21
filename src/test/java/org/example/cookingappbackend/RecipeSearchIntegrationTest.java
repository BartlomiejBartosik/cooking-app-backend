package org.example.cookingappbackend.integration;

import org.example.cookingappbackend.model.*;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.example.cookingappbackend.repository.RecipeRepository;
import org.example.cookingappbackend.repository.UserRepository;
import org.example.cookingappbackend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecipeSearchIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    JwtService jwtService;

    @Test
    void search_byQuery_returnsMatchingRecipe() throws Exception {
        User user = new User();
        user.setEmail("u-" + UUID.randomUUID() + "@test.com");
        user.setName("Jan");
        user.setSurname("Kowalski");
        user.setPassword("x");
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        Ingredient ing = new Ingredient();
        ing.setName("Sugar-" + UUID.randomUUID());
        ing.setUnit("g");
        ing = ingredientRepository.save(ing);

        String title = "Szarlotka-" + UUID.randomUUID();

        Recipe r = new Recipe();
        r.setTitle(title);
        r.setDescription("desc");
        r.setTotalTimeMin(30);

        RecipeIngredient ri = new RecipeIngredient();
        ri.setRecipe(r);
        ri.setIngredient(ing);
        ri.setAmount(200.0);
        r.getIngredients().add(ri);

        RecipeStep step = new RecipeStep();
        step.setRecipe(r);
        step.setStepNo(1);
        step.setInstruction("mix");
        step.setTimeMin(5);
        r.getSteps().add(step);

        recipeRepository.save(r);

        mockMvc.perform(get("/api/recipes/search")
                        .header("Authorization", "Bearer " + token)
                        .param("q", "szarlotka")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.title == '%s')]".formatted(title)).exists());
    }
}
