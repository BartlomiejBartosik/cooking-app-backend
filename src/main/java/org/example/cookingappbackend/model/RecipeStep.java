package org.example.cookingappbackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "recipe_steps",
        indexes = @Index(name = "idx_recipe_steps_recipe_id_step_no", columnList = "recipe_id, stepNo"))
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @JsonBackReference("recipe-steps")
    private Recipe recipe;

    @Column(nullable = false)
    private Integer stepNo;

    @Column(nullable = false, columnDefinition = "text")
    private String instruction;

    private Integer timeMin;
}