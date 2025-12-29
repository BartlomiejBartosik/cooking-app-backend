package org.example.cookingappbackend.repository;

import org.example.cookingappbackend.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Recipe> findAllByOrderByAvgRatingDesc(Pageable pageable);

    @Query("""
        select r from Recipe r
        join r.ingredients ri
        join ri.ingredient i
        where lower(i.name) in :names
        group by r
        having count(distinct(i.name)) >= :minCount
    """)
    Page<Recipe> searchByIngredientNames(@Param("names") List<String> namesLower, @Param("minCount") long minCount, Pageable pageable);
    @Query("""
     select distinct r from Recipe r
     left join fetch r.ingredients ri
     left join fetch ri.ingredient
  """)
    List<Recipe> findAllWithIngredients();
    @Query("""
     select distinct r from Recipe r
     left join fetch r.ingredients ri
     left join fetch ri.ingredient
     where r.id = :id
  """)
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);
    @Query("""
  select r
  from Recipe r
  join r.ingredients ri
  join ri.ingredient i
  group by r
  having sum(case when lower(i.name) in :names then 1 else 0 end) >= :minCount
  order by
    (count(ri) - sum(case when lower(i.name) in :names then 1 else 0 end)) asc,
    sum(case when lower(i.name) in :names then 1 else 0 end) desc,
    coalesce(r.avgRating, 0) desc,
    coalesce(r.totalTimeMin, 2147483647) asc,
    r.title asc
""")
    Page<Recipe> searchPantryRanked(@Param("names") List<String> namesLower, @Param("minCount") long minCount, Pageable pageable);
}