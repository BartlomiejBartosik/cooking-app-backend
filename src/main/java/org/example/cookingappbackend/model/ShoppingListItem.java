package org.example.cookingappbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "shopping_list_items")
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.LAZY)
    private Ingredient ingredient;

    @Column(nullable = false)
    private String name;

    private Double amount;

    private String unit;
}