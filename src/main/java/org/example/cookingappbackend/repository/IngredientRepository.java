package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("""
        select i from Ingredient i
        where lower(i.name) like concat(lower(:q), '%')
           or lower(i.name) like concat('%', lower(:q), '%')
        order by
          case when lower(i.name) like concat(lower(:q), '%') then 0 else 1 end,
          length(i.name) asc,
          i.name asc
    """)
    List<Ingredient> searchRanked(@Param("q") String q);
}
