package org.example.cookingappbackend.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.model.Recipe;
import org.example.cookingappbackend.model.RecipeIngredient;
import org.example.cookingappbackend.model.RecipeStep;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Component
public class SeedLoader {
    private static final Logger log = LoggerFactory.getLogger(SeedLoader.class);

    private final EntityManager em;

    public SeedLoader(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void runOnce() {
        try {
            Long count = em.createQuery("select count(r.id) from Recipe r", Long.class).getSingleResult();
            if (count != null && count > 0) {
                log.info("Seed: recipes already present ({}), skipping.", count);
                return;
            }

            ClassPathResource res = new ClassPathResource("seeds/recipes_seed.json");
            if (!res.exists()) {
                throw new IllegalStateException("Nie znaleziono pliku resources/seeds/recipes_seed.json");
            }

            ObjectMapper om = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try (InputStream is = new ClassPathResource("seeds/recipes_seed.json").getInputStream()) {
                RecipeSeedDto[] arr = om.readValue(is, RecipeSeedDto[].class);
                List<RecipeSeedDto> seeds = Arrays.asList(arr);

                for (RecipeSeedDto s : seeds) {
                    Recipe r = new Recipe();
                    safeSet(r, "setTitle", String.class, s.title);
                    safeSet(r, "setDescription", String.class, s.description);
                    safeSet(r, "setTotalTimeMin", Integer.class, s.totalTimeMin);
                    safeSet(r, "setAvgRating", Double.class, s.avgRating);
                    em.persist(r);

                    for (Ing ing : s.ingredients) {
                        Object categoryValue = resolveCategoryValue(ing.category);
                        Ingredient ingredient = findOrCreateIngredient(ing.ingredientName, ing.unit, categoryValue);

                        RecipeIngredient ri = new RecipeIngredient();
                        safeSet(ri, "setRecipe", Recipe.class, r);
                        safeSet(ri, "setIngredient", Ingredient.class, ingredient);
                        // amount = double
                        safeSet(ri, "setAmount", Double.class, ing.amount != null ? ing.amount : 0.0);
                        em.persist(ri);
                    }

                    for (Step st : s.steps) {
                        RecipeStep step = new RecipeStep();
                        safeSet(step, "setRecipe", Recipe.class, r);
                        safeSet(step, "setStepNo", Integer.class, st.stepNo);
                        safeSet(step, "setInstruction", String.class, st.instruction);
                        safeSet(step, "setTimeMin", Integer.class, st.timeMin);
                        em.persist(step);
                    }
                }

                log.info("Seed: załadowano {} przepisów.", seeds.size());
            }
        } catch (Exception e) {
            log.error("Seed: błąd podczas wstawiania danych", e);
            throw new RuntimeException("Seeding failed", e);
        }
    }


    private void safeSet(Object target, String setterName, Class<?> argType, Object value) {
        try {
            Method m = target.getClass().getMethod(setterName, argType);
            m.invoke(target, value);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ex) {
            throw new RuntimeException("Nie udało się wywołać " + setterName + " na " + target.getClass().getSimpleName(), ex);
        }
    }

    private Object resolveCategoryValue(String catStr) {
        try {
            Field f = Ingredient.class.getDeclaredField("category");
            Class<?> type = f.getType();
            if (type.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Object enumVal = Enum.valueOf((Class<Enum>) type, catStr.toUpperCase(Locale.ROOT));
                return enumVal;
            } else {
                return catStr;
            }
        } catch (Exception e) {
            return catStr;
        }
    }

    private Ingredient findOrCreateIngredient(String name, String unit, Object categoryValue) {
        List<Ingredient> list = em.createQuery(
                        "select i from Ingredient i where lower(i.name)=:n and i.unit=:u and i.category=:c", Ingredient.class)
                .setParameter("n", name.trim().toLowerCase())
                .setParameter("u", unit)
                .setParameter("c", categoryValue)
                .getResultList();

        if (!list.isEmpty()) return list.get(0);

        Ingredient i = new Ingredient();
        safeSet(i, "setName", String.class, name.trim());
        safeSet(i, "setUnit", String.class, unit);
        try {
            Method m;
            try {
                m = i.getClass().getMethod("setCategory", categoryValue.getClass());
            } catch (NoSuchMethodException e) {
                m = i.getClass().getMethod("setCategory", String.class);
                categoryValue = categoryValue.toString();
            }
            m.invoke(i, categoryValue);
        } catch (Exception ex) {
            throw new RuntimeException("Nie udało się ustawić kategorii na Ingredient", ex);
        }
        em.persist(i);
        return i;
    }

    public static class RecipeSeedDto {
        public String title;
        public String description;
        public Integer totalTimeMin;
        public Double avgRating;
        public List<Ing> ingredients;
        public List<Step> steps;
    }
    public static class Ing {
        public String ingredientName;
        public String unit;
        public String category;
        public Double amount;
    }
    public static class Step {
        public Integer stepNo;
        public String instruction;
        public Integer timeMin;
    }
}
