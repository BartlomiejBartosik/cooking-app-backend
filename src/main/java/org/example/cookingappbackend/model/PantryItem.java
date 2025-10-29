package org.example.cookingappbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pantry_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_pantry_user_ingredient", columnNames = {"user_id","ingredient_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PantryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "ingredient_category")
    private String ingredientCategory;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch() { this.updatedAt = LocalDateTime.now(); }
}
