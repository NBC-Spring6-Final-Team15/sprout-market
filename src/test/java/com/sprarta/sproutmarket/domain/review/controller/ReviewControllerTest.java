//package com.sprarta.sproutmarket.domain.review.controller;
//
//
//import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
//import com.epages.restdocs.apispec.ResourceSnippetParameters;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sprarta.sproutmarket.config.JwtAuthenticationFilter;
//import com.sprarta.sproutmarket.config.JwtUtil;
//import com.sprarta.sproutmarket.config.SecurityConfig;
//import com.sprarta.sproutmarket.domain.review.dto.ReviewRequestDto;
//import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
//import com.sprarta.sproutmarket.domain.review.enums.ReviewRating;
//import com.sprarta.sproutmarket.domain.review.service.ReviewService;
//import com.sprarta.sproutmarket.domain.user.dto.response.UserResponse;
//import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
//import com.sprarta.sproutmarket.domain.user.entity.User;
//import com.sprarta.sproutmarket.domain.user.enums.UserRole;
//import com.sprarta.sproutmarket.domain.user.service.CustomUserDetailService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.RestDocumentationExtension;
//import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
//import org.springframework.restdocs.payload.JsonFieldType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.List;
//
//import static com.epages.restdocs.apispec.ResourceDocumentation.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
//import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ReviewController.class)
//@Import(SecurityConfig.class)
//@AutoConfigureRestDocs
//@ExtendWith(RestDocumentationExtension.class)
//@MockBean(JpaMetamodelMappingContext.class)
//@AutoConfigureMockMvc(addFilters = false)
//class ReviewControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ReviewService reviewService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    JwtUtil jwtUtil;
//
//    @MockBean
//    CustomUserDetailService customUserDetailService;
//
//    @BeforeEach
//    void setUp() {
//        User user = new User(1L, "username", "email@example.com", "encodedOldPassword", "nickname", "010-1234-5678", "address", UserRole.USER);
//        CustomUserDetails mockAuthUser = new CustomUserDetails(user);
//
//        // 인증 유저 시큐리티 컨텍스트 홀더에 저장
//        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mockAuthUser, null, mockAuthUser.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//    @Test
//    void 리뷰_생성_성공() throws Exception {
//        // given
//        Long tradeId = 1L;
//        ReviewRequestDto reviewRequestDto = new ReviewRequestDto("좋아요", ReviewRating.GOOD);
//        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(1L, tradeId, "좋아요", ReviewRating.GOOD);
//
//        when(reviewService.createReview(any(Long.class), any(ReviewRequestDto.class), any(CustomUserDetails.class)))
//                .thenReturn(reviewResponseDto);
//
//        // when, then
//        mockMvc.perform(post("/reviews/{tradeId}", tradeId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reviewRequestDto)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.comment").value(reviewRequestDto.getComment()))
//                .andExpect(jsonPath("$.data.reviewRating").value("GOOD"))
//                .andDo(document("reviews/create",  // 문서화 이름
//                        responseFields(
//                                fieldWithPath("message").type(JsonFieldType.STRING)
//                                        .description("응답 메시지"),
//                                fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
//                                        .description("HTTP 상태 코드"),
//                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
//                                        .description("리뷰 ID"),
//                                fieldWithPath("data.tradeId").type(JsonFieldType.NUMBER)
//                                        .description("거래 ID"),
//                                fieldWithPath("data.comment").type(JsonFieldType.STRING)
//                                        .description("리뷰 내용"),
//                                fieldWithPath("data.reviewRating").type(JsonFieldType.STRING)
//                                        .description("리뷰 평점")
//                        )
//                ));
//
//    }
//
//    @Test
//    void 리뷰_조회_성공() throws Exception {
//        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(1L, 2L, "좋아요", ReviewRating.GOOD);
//
//        when(reviewService.getReview(1L)).thenReturn(reviewResponseDto);
//
//        mockMvc.perform(RestDocumentationRequestBuilders.get("/reviews/{reviewId}", 1L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.comment").value(reviewResponseDto.getComment()))
//                .andExpect(jsonPath("$.data.reviewRating").value("GOOD"))
//                .andDo(MockMvcRestDocumentationWrapper.document(
//                        "get-review",
//                        resource(ResourceSnippetParameters.builder()
//                                .description("특정 리뷰의 정보를 조회합니다.")
//                                .pathParameters(
//                                        parameterWithName("reviewId").description("조회할 리뷰 ID")
//                                )
//                                .summary("리뷰 조회")
//                                .tag("Review")
//                                .responseFields(List.of(
//                                        fieldWithPath("message").type(JsonFieldType.STRING)
//                                                .description("응답 메시지"),
//                                        fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
//                                                .description("HTTP 상태 코드"),
//                                        fieldWithPath("data.id").type(JsonFieldType.NUMBER)
//                                                .description("리뷰 ID"),
//                                        fieldWithPath("data.tradeId").type(JsonFieldType.NUMBER)
//                                                .description("관련 거래 ID"),
//                                        fieldWithPath("data.comment").type(JsonFieldType.STRING)
//                                                .description("리뷰 내용"),
//                                        fieldWithPath("data.reviewRating").type(JsonFieldType.STRING)
//                                                .description("리뷰 등급 (GOOD, BAD 등)"),
//                                        fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
//                                                .optional().description("리뷰 작성 시간")
//                                ))
//                                .responseHeaders(
//                                        headerWithName("Content-Type").description("응답의 Content-Type")
//                                )
//                                .build()
//                        )
//                ));
//
//        verify(reviewService).getReview(1L);
//    }
//
//
//}
