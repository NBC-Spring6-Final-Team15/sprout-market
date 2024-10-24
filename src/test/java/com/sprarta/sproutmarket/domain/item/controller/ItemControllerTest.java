package com.sprarta.sproutmarket.domain.item.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.config.JwtUtil;
import com.sprarta.sproutmarket.config.SecurityConfig;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemContentsUpdateRequest;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.service.ItemService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.service.CustomUserDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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

    @Test
    @WithMockUser
    void 매물_수정_성공() throws Exception {
        User user = new User();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        ItemResponse itemResponse = new ItemResponse("만년필","한번도안썼습니다",3000,"김커피");
        ItemContentsUpdateRequest requestDto = new ItemContentsUpdateRequest("만년필","한번도안썼습니다",3000,"imageUrl");
        Long itemId = 1L;
        given(itemService.updateContents(itemId,requestDto,customUserDetails)).willReturn(itemResponse);

        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.post("/items/{itemId}/update/contents",itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "update-Contents",
                                resource(ResourceSnippetParameters.builder()
                                        .description("매물의 정보를 변경합니다.")
                                        .pathParameters(
                                                parameterWithName("id").description("수정할 매물 ID")
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
                                        .requestSchema(Schema.schema("update-contents-request"))
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
                                        .responseSchema(Schema.schema("update-contents-response"))
                                        .build()
                                )
                        )
                );

        verify(itemService, times(1)).updateContents(any(Long.class),any(ItemContentsUpdateRequest.class),any(CustomUserDetails.class));
        result.andExpect(status().isOk());
    }
}