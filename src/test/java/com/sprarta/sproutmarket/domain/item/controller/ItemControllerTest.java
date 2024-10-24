package com.sprarta.sproutmarket.domain.item.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.config.JwtUtil;
import com.sprarta.sproutmarket.config.SecurityConfig;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.item.dto.request.FindItemsInMyAreaRequestDto;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemContentsUpdateRequest;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponseDto;
import com.sprarta.sproutmarket.domain.item.entity.ItemSaleStatus;
import com.sprarta.sproutmarket.domain.item.service.ItemService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.service.CustomUserDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {
    @MockBean
    ItemService itemService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    CustomUserDetailService customUserDetailService;

    @MockBean
    JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private CustomUserDetails mockAuthUser;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1L, "username", "email@example.com", "encodedOldPassword", "nickname", "010-1234-5678", "address", UserRole.USER);
        CustomUserDetails mockAuthUser = new CustomUserDetails(mockUser);

        // 인증 유저 시큐리티 컨텍스트 홀더에 저장
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mockAuthUser, null, mockAuthUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @WithMockUser
    void 매물_수정_성공() throws Exception {
        ItemResponse itemResponse = new ItemResponse("만년필","한번도안썼습니다",3000,"김커피");
        ItemContentsUpdateRequest requestDto = new ItemContentsUpdateRequest("만년필","한번도안썼습니다",3000,"imageUrl");
        Long itemId = 1L;
        given(itemService.updateContents(any(Long.class),any(ItemContentsUpdateRequest.class),any(CustomUserDetails.class))).willReturn(itemResponse);

        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.post("/items/{itemId}/update/contents",itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "update-Contents",
                                resource(ResourceSnippetParameters.builder()
                                        .description("매물의 정보를 변경합니다.")
                                        .pathParameters(
                                                parameterWithName("itemId").description("수정할 매물 ID")
                                        )
                                        .summary("매물 정보 업데이트")
                                        .tag("Item")
                                        .requestFields(List.of(
                                                fieldWithPath("title").type(JsonFieldType.STRING)
                                                        .description("수정할 제목"),
                                                fieldWithPath("description").type(JsonFieldType.STRING)
                                                        .description("수정할 내용"),
                                                fieldWithPath("price").type(JsonFieldType.NUMBER)
                                                        .description("수정할 가격"),
                                                fieldWithPath("imageUrl").type(JsonFieldType.STRING)
                                                        .description("변경할 이미지")
                                        ))
                                        .requestHeaders(
                                                headerWithName("Authorization")
                                                        .description("Bearer (JWT 토큰)")
                                        )
                                        .requestSchema(Schema.schema("매물-수정-성공-요청"))
                                        .responseFields(
                                                fieldWithPath("message").type(JsonFieldType.STRING)
                                                        .description("성공 시 메시지"),
                                                fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
                                                        .description("200 상태 코드"),
                                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                                        .description("반환된 정보"),
                                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                                        .description("수정된 제목"),
                                                fieldWithPath("data.description").type(JsonFieldType.STRING)
                                                        .description("수정된 내용"),
                                                fieldWithPath("data.price").type(JsonFieldType.NUMBER)
                                                        .description("수정된 가격"),
                                                fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                                                        .description("수정한 유저 닉네임")
                                        )
                                        .responseSchema(Schema.schema("매물-수정-성공-응답"))
                                        .build()
                                )
                        )
                );

        verify(itemService, times(1)).updateContents(any(Long.class),any(ItemContentsUpdateRequest.class),any(CustomUserDetails.class));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(itemResponse.getTitle()))
                .andExpect(jsonPath("$.data.description").value(itemResponse.getDescription()))
                .andExpect(jsonPath("$.data.price").value(itemResponse.getPrice()));
    }

    @Test
    @WithMockUser
    void 내_주변_매물_조회_성공() throws Exception {
        FindItemsInMyAreaRequestDto requestDto = new FindItemsInMyAreaRequestDto(1,10);
        //페이지 직접 만들어주기
        List<ItemResponseDto> dtoList = new ArrayList<>();
        for(int i = 1; i <= 2; i++) {
            ItemResponseDto dto = new ItemResponseDto(
                    (long) i, //
                    "제목" + i,
                    "내용" + i,
                    15000,
                    "닉네임" + i,
                    ItemSaleStatus.WAITING,
                    "카테고리 이름" + i,
                    Status.ACTIVE
            );
            dtoList.add(dto);
        }

        Pageable pageable = PageRequest.of(0,10);
        Page<ItemResponseDto> pageResult = new PageImpl<>(dtoList,pageable,2);

        given(itemService.findItemsByMyArea(any(CustomUserDetails.class),any(FindItemsInMyAreaRequestDto.class))).willReturn(pageResult);
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/items/myAreas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(MockMvcRestDocumentationWrapper.document(
                        "get-items-by-my-areas",
                        resource(ResourceSnippetParameters.builder()
                                .description("우리 동네의 매물을 조회합니다.")
                                .summary("동네 매물 조회")
                                .tag("item")
                                .requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Bearer (JWT 토큰)")
                                )
                                .requestFields(
                                        fieldWithPath("page").type(JsonFieldType.NUMBER)
                                                .description("페이지 넘버, 기본값 1, 최소값 1"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("페이지 크기, 기본값 10, 최소값 1")
                                )
                                .responseFields(
                                        fieldWithPath("message").type(JsonFieldType.STRING)
                                                .description("성공 상태 메시지"),
                                        fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
                                                .description("성공 시 응답 코드 : 200"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                                                .description("응답 본문"),
                                        fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                                                .description("아이템 리스트"),
                                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER)
                                                .description("아이템 ID"),
                                        fieldWithPath("data.content[].title").type(JsonFieldType.STRING)
                                                .description("아이템 제목"),
                                        fieldWithPath("data.content[].description").type(JsonFieldType.STRING)
                                                .description("아이템 설명"),
                                        fieldWithPath("data.content[].price").type(JsonFieldType.NUMBER)
                                                .description("아이템 가격"),
                                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                                                .description("판매자 닉네임"),
                                        fieldWithPath("data.content[].itemSaleStatus").type(JsonFieldType.STRING)
                                                .description("아이템 판매 상태"),
                                        fieldWithPath("data.content[].categoryName").type(JsonFieldType.STRING)
                                                .description("아이템 카테고리 이름"),
                                        fieldWithPath("data.content[].status").type(JsonFieldType.STRING)
                                                .description("아이템 상태 (예: ACTIVE, INACTIVE)"),
                                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT)
                                                .description("페이지 관련 정보"),
                                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호 (0부터 시작)"),
                                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT)
                                                .description("정렬 정보"),
                                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN)
                                                .description("정렬 정보가 비어 있는지 여부"),
                                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN)
                                                .description("정렬되었는지 여부"),
                                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN)
                                                .description("정렬되지 않았는지 여부"),
                                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER)
                                                .description("현재 페이지의 시작점 오프셋"),
                                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN)
                                                .description("페이징 여부"),
                                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN)
                                                .description("페이징되지 않은 여부"),
                                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER)
                                                .description("전체 페이지 수"),
                                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER)
                                                .description("전체 아이템 수"),
                                        fieldWithPath("data.size").type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.number").type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호"),
                                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT)
                                                .description("현재 페이지의 정렬 정보"),
                                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN)
                                                .description("현재 페이지의 정렬 정보가 비어 있는지 여부"),
                                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN)
                                                .description("현재 페이지가 정렬되었는지 여부"),
                                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN)
                                                .description("현재 페이지가 정렬되지 않았는지 여부"),
                                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN)
                                                .description("첫 번째 페이지인지 여부"),
                                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER)
                                                .description("현재 페이지에 있는 아이템 수"),
                                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN)
                                                .description("현재 페이지가 비어 있는지 여부")
                                )
                                .requestSchema(Schema.schema("동네-매물-조회-성공-요청"))
                                .responseSchema(Schema.schema("동네-매물-조회-성공-응답"))
                                .build()
                        )
                ));

        verify(itemService,times(1)).findItemsByMyArea(any(CustomUserDetails.class),any(FindItemsInMyAreaRequestDto.class));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }


}