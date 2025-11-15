package org.example.cookingappbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.cookingappbackend.enums.IngredientCategory;

@Data
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "ingredients",
        uniqueConstraints = @UniqueConstraint(name = "uk_ingredient_name", columnNames = "name"))
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private IngredientCategory category;
}