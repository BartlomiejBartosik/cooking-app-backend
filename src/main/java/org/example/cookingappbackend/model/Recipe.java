package org.example.cookingappbackend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    private Integer totalTimeMin;

    private Double avgRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @JsonManagedReference("recipe-ingredients")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNo ASC")
    @JsonManagedReference("recipe-steps")
    private List<RecipeStep> steps = new ArrayList<>();

    public void addIngredient(RecipeIngredient ri) {
        ingredients.add(ri);
        ri.setRecipe(this);
    }
    public void removeIngredient(RecipeIngredient ri) {
        ingredients.remove(ri);
        ri.setRecipe(null);
    }
    public void addStep(RecipeStep step) {
        steps.add(step);
        step.setRecipe(this);
    }
    public void removeStep(RecipeStep step) {
        steps.remove(step);
        step.setRecipe(null);
    }
}
