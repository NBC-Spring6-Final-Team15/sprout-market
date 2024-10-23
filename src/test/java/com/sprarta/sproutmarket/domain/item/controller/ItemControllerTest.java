package com.sprarta.sproutmarket.domain.item.controller;


import com.sprarta.sproutmarket.domain.category.entity.Category;
import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemCreateRequest;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.service.ItemService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class ItemControllerTest {
    @InjectMocks
    private ItemController itemController;

    @Mock
    private ItemService itemService;

    @Mock
    private CustomUserDetails authUser;

    private User mockUser;

    private Category mockCategory;


    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this); // 초기화

        // User 생성
        mockUser = new User(
            "가짜 객체1",
            "mock@mock.com",
            "Mock1234!",
            "오만한천원",
            "01012341234",
            "서울시 노원구 공릉동",
            UserRole.USER
        );
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        // 카테고리 생성(id=1L)
        mockCategory = new Category(1L, "생활");



    }

    @Test
    void 매물_등록_성공(){
        // Given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
            "가짜11",
            "설명11",
            100,
            1L,
            ""
        );
        ItemResponse itemResponse = new ItemResponse(
            "가짜11",
            100,
            "오만한천원"
        );

        when(itemService.createItem(any(ItemCreateRequest.class), any(CustomUserDetails.class)))
            .thenReturn(itemResponse);

        // When
        ResponseEntity<ApiResponse<ItemResponse>> response = itemController.addItem(itemCreateRequest, authUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("가짜11", response.getBody().getData().getTitle());
        verify(itemService, times(1)).createItem(any(ItemCreateRequest.class), any(CustomUserDetails.class));
    }
}