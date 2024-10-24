package com.sprarta.sproutmarket.domain.review.controller;


import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.report.dto.ReportResponseDto;
import com.sprarta.sproutmarket.domain.review.dto.ReviewRequestDto;
import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
import com.sprarta.sproutmarket.domain.review.service.ReviewService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    // 생성
    @PostMapping("/reviews/{tradeId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @PathVariable Long tradeId,
            @RequestBody @Valid ReviewRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        ReviewResponseDto responseDto = reviewService.createReview(tradeId, dto, customUserDetails);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 단건 조회
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> getReview(
            @PathVariable Long reviewId
    ) {
        ReviewResponseDto responseDto = reviewService.getReview(reviewId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 유저 리뷰 전체 조회
    @GetMapping("/reviews/users/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getReviews(
            @PathVariable Long userId
    ) {
        List<ReviewResponseDto> responseDto = reviewService.getReviews(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 수정
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, dto, customUserDetails);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        reviewService.deleteReview(reviewId, customUserDetails);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }


}


