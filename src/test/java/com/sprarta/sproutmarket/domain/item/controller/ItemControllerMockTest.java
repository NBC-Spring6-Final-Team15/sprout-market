package com.sprarta.sproutmarket.domain.item.controller;

import com.sprarta.sproutmarket.domain.category.entity.Category;
import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemContentsUpdateRequest;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemCreateRequest;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponseDto;
import com.sprarta.sproutmarket.domain.item.entity.ItemSaleStatus;
import com.sprarta.sproutmarket.domain.item.service.ItemService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class ItemControllerMockTest {
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

    @Test
    void 매물_판매상태_변경_성공() {
        Long itemId = 1L;
        String saleStatus = "SOLD";
        ItemResponse response = new ItemResponse(
            "가짜11",
            "설명11",
            100,
            "오만한천원"
        );
        when(itemService.updateSaleStatus(eq(itemId), eq(saleStatus), eq(authUser))).thenReturn(response);

        ResponseEntity<ApiResponse<ItemResponse>> result = itemController.updateItemSaleStatus(itemId, saleStatus, authUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void 매물_내용_변경_성공() {
        Long itemId = 1L;
        ItemContentsUpdateRequest request = new ItemContentsUpdateRequest(
            "가짜22",
            "설명22",
            450,
            ""
        );
        ItemResponse response = new ItemResponse(
            "가짜22",
            "설명22",
            450,
            "오만한천원"
        );
        when(itemService.updateContents(itemId, request, authUser)).thenReturn(response);

        ResponseEntity<ApiResponse<ItemResponse>> result = itemController.updateContent(itemId, request, authUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void 나의_매물_논리적삭제_성공() {
        Long itemId = 1L;
        ItemResponse response = new ItemResponse(
            "가짜11",
            Status.DELETED,
            "오만한천원"
        );
        when(itemService.softDeleteItem(eq(itemId), eq(authUser))).thenReturn(response);

        ResponseEntity<ApiResponse<ItemResponse>> result = itemController.softRemoveItem(itemId, authUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void 관리자_신고매물_논리적삭제_성공() {
        Long itemId = 1L;
        ItemResponse response = new ItemResponse(
            "가짜11",
            "설명11",
            Status.DELETED
        );
        when(itemService.softDeleteReportedItem(eq(itemId), eq(authUser))).thenReturn(response);

        ResponseEntity<ApiResponse<ItemResponse>> result = itemController.softRemoveReportedItem(itemId, authUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void 매물_단건_상세_조회_성공() {
        // Given
        Long itemId = 1L;
        ItemResponseDto itemResponseDto = new ItemResponseDto(
            1L,
            "가짜11",
            "설명11",
            1000,
            "오만한천원",
            ItemSaleStatus.WAITING,
            "생활",
            Status.ACTIVE
        );
        when(itemService.getItem(anyLong())).thenReturn(itemResponseDto);

        // When
        ResponseEntity<ApiResponse<ItemResponseDto>> response = itemController.findItem(itemId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemResponseDto, response.getBody().getData());
    }

    @Test
    void 사용자의_모든매물_조회_성공() {
        int page = 1;
        int size = 10;
        Page<ItemResponseDto> itemResponseDto = mock(Page.class);
        when(itemService.getMyItems(eq(page), eq(size), eq(authUser))).thenReturn(itemResponseDto);

        ResponseEntity<ApiResponse<Page<ItemResponseDto>>> result = itemController.findMyItems(page, size, authUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(itemResponseDto, result.getBody().getData());
    }



    @Test
    void  특정_카테고리_전체_조회_성공() {
        int page = 1;
        int size = 10;
        Long categoryId = 1L;
        Page<ItemResponseDto> itemResponseDto = mock(Page.class);
        when(itemService.getCategoryItems(page, size, categoryId)).thenReturn(itemResponseDto);

        ResponseEntity<ApiResponse<Page<ItemResponseDto>>> result = itemController.getCategoryItems(page, size, categoryId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(itemResponseDto, result.getBody().getData());
    }

}