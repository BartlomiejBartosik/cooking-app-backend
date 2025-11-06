package org.example.cookingappbackend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "favorite",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recipe_id"}))
public class Favorite {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Favorite() {}
    public Favorite(Long userId, Recipe recipe) {
        this.userId = userId;
        this.recipe = recipe;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Recipe getRecipe() { return recipe; }
    public Instant getCreatedAt() { return createdAt; }
}
