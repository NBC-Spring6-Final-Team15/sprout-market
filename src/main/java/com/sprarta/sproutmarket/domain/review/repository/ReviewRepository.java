package com.sprarta.sproutmarket.domain.review.repository;

import com.sprarta.sproutmarket.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
