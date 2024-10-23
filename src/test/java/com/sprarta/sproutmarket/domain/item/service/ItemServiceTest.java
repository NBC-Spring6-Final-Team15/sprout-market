package com.sprarta.sproutmarket.domain.item.service;

import com.sprarta.sproutmarket.domain.category.entity.Category;
import com.sprarta.sproutmarket.domain.category.service.CategoryService;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemCreateRequest;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.entity.ItemSaleStatus;
import com.sprarta.sproutmarket.domain.item.repository.ItemRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ItemServiceTest {

    // 가짜 객체 사용
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ItemService itemService;

    private User mockUser;
    private Category mockCategory1;
    private Category mockCategory2;
    private Item mockItem1;
    private Item mockItem2;
    private CustomUserDetails authUser;

    @BeforeEach // 코드 실행 전 작동 + 테스트 환경 초기화
    void setup(){
        MockitoAnnotations.openMocks(this); //어노테이션 Mock과 InjectMocks를 초기화

        // 가짜 사용자 생성
        //String username, String email, String password, String nickname, String phoneNumber, String address, UserRole userRole
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

        // 가짜 카테고리 생성
        mockCategory1 = new Category(1L, "생활");
        mockCategory2 = new Category(2L, "가구");

        // 가짜 매물 생성
        mockItem1 = Item.builder()
            .title("가짜 매물1")
            .description("가짜 설명1")
            .price(10000)
            .itemSaleStatus(ItemSaleStatus.WAITING)
            .seller(mockUser)
            .category(mockCategory1)
            .status(Status.ACTIVE)
            .build();
        ReflectionTestUtils.setField(mockItem1, "id", 1L);

        mockItem2 = Item.builder()
            .title("가짜 매물2")
            .description("가짜 설명2")
            .price(3000)
            .itemSaleStatus(ItemSaleStatus.WAITING)
            .seller(mockUser)
            .category(mockCategory2)
            .status(Status.ACTIVE)
            .build();
        ReflectionTestUtils.setField(mockItem2, "id", 2L);

        // CustomUserDetails(사용자 정보) 모킹 => 로그인된 사용자의 정보 모킹
        authUser = mock(CustomUserDetails.class);
        when(authUser.getId()).thenReturn(mockUser.getId()); // authUser의 ID를 mockUser의 ID로 설정
        when(authUser.getEmail()).thenReturn("mock@mock.com");
        when(authUser.getRole()).thenReturn(UserRole.USER);

        // itemRepository.save() 호출 시 mockItem2를 반환하도록 설정
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem2);

        // userRepository.findById() 호출 시 mockUser를 반환하도록 설정
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        // itemRepository.findById() 호출 시 mockItem1과 mockItem2를 반환하도록 설정
        when(itemRepository.findById(mockItem1.getId())).thenReturn(Optional.of(mockItem1));
        when(itemRepository.findById(mockItem2.getId())).thenReturn(Optional.of(mockItem2));
    }

    @Test
    void 매물_생성_성공(){
        // Given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
            "가짜 매물1",
            "가짜 설명1",
            10000,
            1L,
            ""
        );

        when(userRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem1);
        when(categoryService.findByIdOrElseThrow(mockCategory1.getId())).thenReturn(mockCategory1);

        // When
        ItemResponse itemResponse = itemService.createItem(itemCreateRequest, authUser);

        // Then
        assertEquals("가짜 매물1", itemResponse.getTitle());
        assertEquals(10000, itemResponse.getPrice());
        assertEquals("오만한천원", itemResponse.getNickname());
    }

//    @Test
//    void 매물_판매상태_변경_성공() {
//        // Given
//        String newSaleStatus = "SOLD";
//
//        // userRepository에서 m  ockUser를 반환하도록 설정
//        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
//
//        // itemRepository에서 mockItem2를 반환하도록 설정
//        when(itemRepository.findById(mockItem2.getId())).thenReturn(Optional.of(mockItem2));
//
//        // findByIdAndSellerIdOrElseThrow 메서드가 mockItem2를 반환하도록 설정
//        when(itemService.findByIdAndSellerIdOrElseThrow(mockItem2.getId(), mockUser)).thenReturn(mockItem2);
//
//        // save 메서드 호출 시 mockItem2를 반환하도록 설정
//        when(itemRepository.save(any(Item.class))).thenReturn(mockItem2);
//
//        // When
//        ItemResponse itemResponse = itemService.updateSaleStatus(mockItem2.getId(), newSaleStatus, authUser);
//
//        // Then
//        assertEquals("가짜 매물2", itemResponse.getTitle());
//        assertEquals(3000, itemResponse.getPrice());
//        assertEquals(ItemSaleStatus.SOLD, itemResponse.getItemSaleStatus());
//        assertEquals("오만한천원", itemResponse.getNickname());
//    }




}