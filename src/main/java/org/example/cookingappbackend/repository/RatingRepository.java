package org.example.cookingappbackend.repository;
import org.example.cookingappbackend.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {}
