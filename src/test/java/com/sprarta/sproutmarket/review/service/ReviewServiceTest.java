package com.sprarta.sproutmarket.review.service;


import com.sprarta.sproutmarket.domain.category.entity.Category;
import com.sprarta.sproutmarket.domain.category.repository.CategoryRepository;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.entity.ItemSaleStatus;
import com.sprarta.sproutmarket.domain.review.dto.ReviewRequestDto;
import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
import com.sprarta.sproutmarket.domain.review.entity.Review;
import com.sprarta.sproutmarket.domain.review.enums.ReviewRating;
import com.sprarta.sproutmarket.domain.review.repository.ReviewRepository;
import com.sprarta.sproutmarket.domain.review.service.ReviewService;
import com.sprarta.sproutmarket.domain.trade.entity.Trade;
import com.sprarta.sproutmarket.domain.trade.repository.TradeRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void 리뷰_생성_정상동작() {
        // given
        Long tradeId = 1L;

        User seller = Mockito.mock(User.class);

        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getSeller()).thenReturn(seller);

        Trade trade = Mockito.mock(Trade.class);
        Mockito.when(trade.getId()).thenReturn(tradeId);
        Mockito.when(trade.getItem()).thenReturn(item);

        User user = Mockito.mock(User.class);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        ReviewRequestDto dto = new ReviewRequestDto("친절함", ReviewRating.GOOD);

        when(tradeRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(trade));

        // when
        ReviewResponseDto response = reviewService.createReview(tradeId, dto, customUserDetails);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getTradeId());
        assertEquals("친절함", response.getComment());
        assertEquals(ReviewRating.GOOD, response.getReviewRating());

        verify(tradeRepository).findById(tradeId);
        verify(reviewRepository).save(any(Review.class));

        verify(seller).plusRate(); // plusRate가 호출되었는지 확인
        verify(seller, never()).minusRate();
    }

    @Test
    void 리뷰_조회_정상동작() {
        // given
        Long tradeId = 1L;
        Long reviewId = 1L;

        Trade trade = Mockito.mock(Trade.class);
        Mockito.when(trade.getId()).thenReturn(tradeId);

        Review review = Mockito.mock(Review.class);
        Mockito.when(review.getId()).thenReturn(reviewId);
        Mockito.when(review.getTrade()).thenReturn(trade);
        Mockito.when(review.getComment()).thenReturn("친절함");
        Mockito.when(review.getReviewRating()).thenReturn(ReviewRating.GOOD);

        when(reviewRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(review));

        // when
        ReviewResponseDto response = reviewService.getReview(reviewId);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getTradeId());
        assertEquals("친절함", response.getComment());
        assertEquals(ReviewRating.GOOD, response.getReviewRating());

    }



}
