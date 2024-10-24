package com.sprarta.sproutmarket.domain.review.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.config.JwtAuthenticationFilter;
import com.sprarta.sproutmarket.config.JwtUtil;
import com.sprarta.sproutmarket.config.SecurityConfig;
import com.sprarta.sproutmarket.domain.review.dto.ReviewRequestDto;
import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
import com.sprarta.sproutmarket.domain.review.enums.ReviewRating;
import com.sprarta.sproutmarket.domain.review.service.ReviewService;
import com.sprarta.sproutmarket.domain.user.dto.response.UserResponse;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.service.CustomUserDetailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    CustomUserDetailService customUserDetailService;

    @MockBean
    JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @WithMockUser
    void 리뷰_생성_성공() throws Exception {
        // given
        Long tradeId = 1L;
        ReviewRequestDto requestDto = new ReviewRequestDto("좋은 거래였다", ReviewRating.GOOD);
        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(1L,1L,"좋아요",ReviewRating.GOOD);

        User user = Mockito.mock(User.class);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        when(reviewService.createReview(1L, requestDto, customUserDetails))
                .thenReturn(reviewResponseDto);

        // when, then
        mockMvc.perform(post("/reviews/{tradeId}", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))) // Request Body 설정
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comment").value(reviewResponseDto.getComment())) // 반환된 comment 값 확인
                .andExpect(jsonPath("$.data.reviewRating").value(reviewResponseDto.getReviewRating().toString())); // 반환된 reviewRating 값 확인

    }

    @Test
    @WithMockUser
    void 리뷰_조회_성공() throws Exception {
        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(1L,2L,"좋아요",ReviewRating.GOOD);

        when(reviewService.getReview(1L)).thenReturn(reviewResponseDto);

        mockMvc.perform(get("/reviews/{reviewId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comment").value(reviewResponseDto.getComment()))
                .andExpect(jsonPath("$.data.reviewRating").value("GOOD"));

        verify(reviewService).getReview(1L);
    }

}
