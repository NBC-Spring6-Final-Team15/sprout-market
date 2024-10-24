package com.sprarta.sproutmarket.domain.report.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.config.JwtUtil;
import com.sprarta.sproutmarket.config.SecurityConfig;
import com.sprarta.sproutmarket.domain.report.dto.ReportRequestDto;
import com.sprarta.sproutmarket.domain.report.dto.ReportResponseDto;
import com.sprarta.sproutmarket.domain.report.enums.ReportStatus;
import com.sprarta.sproutmarket.domain.report.service.ReportService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.service.CustomUserDetailService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ReportService reportService;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    CustomUserDetailService customUserDetailService;

    @MockBean
    JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    CustomUserDetails mockAuthUser;

    Long itemId;
    String reportingReason;
    ReportRequestDto requestDto;
    ReportResponseDto responseDto;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1L, "username", "email@example.com", "encodedOldPassword", "nickname", "010-1234-5678", "address", UserRole.USER);
        CustomUserDetails mockAuthUser = new CustomUserDetails(mockUser);

        // 인증 유저 스프링 컨텍스트 홀더에 저장
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mockAuthUser, null, mockAuthUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        itemId = 1L;
        reportingReason = "지우개를 5만원에 팔아요";
        requestDto = new ReportRequestDto(reportingReason);
        responseDto = new ReportResponseDto(1L, 1L, reportingReason, ReportStatus.WAITING);
    }

    @Test
    @WithMockUser
    void createReport() throws Exception {
        //given
        when(reportService.createReport(anyLong(), any(ReportRequestDto.class), any(CustomUserDetails.class)))
                .thenReturn(responseDto);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.post("/reports/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(MockMvcRestDocumentationWrapper.document("create-report",
                        resource(ResourceSnippetParameters.builder()
                                .description("특정 매물에 대한 신고를 생성합니다.")
                                .summary("매물 신고 생성")
                                .tag("report")
                                .pathParameters(
                                        parameterWithName("itemId")
                                                .description("신고할 매물 ID")
                                )
                                .requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Bearer (JWT 토큰)")
                                )
                                .requestFields(
                                        fieldWithPath("reportingReason")
                                                .description("신고 사유")
                                )
                                .responseFields(
                                        fieldWithPath("message")
                                                .description("성공 메시지 : Ok"),
                                        fieldWithPath("statusCode")
                                                .description("성공 상태 코드 : 200"),
                                        fieldWithPath("data")
                                                .description("본문 응답"),
                                        fieldWithPath("data.id")
                                                .description("신고 ID"),
                                        fieldWithPath("data.itemId")
                                                .description("신고된 매물 ID"),
                                        fieldWithPath("data.reportingReason")
                                                .description("신고된 사유"),
                                        fieldWithPath("data.reportStatus")
                                                .description("신고 상태 : 기본값 WAITING")
                                )
                                .requestSchema(Schema.schema("신고-생성-성공-요청"))
                                .responseSchema(Schema.schema("신고-생성-성공-응답"))
                                .build()
                        )));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.reportingReason").value(reportingReason));
    }

    @Test
    @WithMockUser
    void getReport() throws Exception {
        Long reportId = 1L;
        when(reportService.getReport(anyLong())).thenReturn(responseDto);
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/reports/{reportId}", reportId)
                .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(MockMvcRestDocumentationWrapper.document(
                        "get-a-report",
                        resource(ResourceSnippetParameters.builder()
                                .description("신고 ID로 신고를 단건조회합니다.")
                                .summary("신고 단건 조회")
                                .tag("report")
                                .requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Bearer (JWT 토큰)")
                                )
                                .pathParameters(
                                        parameterWithName("reportId").description("조회할 신고 ID")
                                )
                                .responseFields(
                                        fieldWithPath("message")
                                                .description("성공 메시지 : Ok"),
                                        fieldWithPath("statusCode")
                                                .description("성공 상태 코드 : 200"),
                                        fieldWithPath("data")
                                                .description("본문 응답"),
                                        fieldWithPath("data.id")
                                                .description("신고 ID"),
                                        fieldWithPath("data.itemId")
                                                .description("신고된 매물 ID"),
                                        fieldWithPath("data.reportingReason")
                                                .description("신고된 사유"),
                                        fieldWithPath("data.reportStatus")
                                                .description("신고 상태 : 기본값 WAITING")
                                )
                                .responseSchema(Schema.schema("신고-단건-조회-성공-응답"))
                                .build()

                        )));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.statusCode").value(200));
        result.andExpect(jsonPath("$.data.reportingReason").value(reportingReason));
    }

    @Test
    @WithMockUser
    void getReports() throws Exception {
        ReportResponseDto responseDto2 = new ReportResponseDto(2L, 1L, "직거래 약속 잡았는데 안 나와요", ReportStatus.WAITING);
        List<ReportResponseDto> responseDtoList = new ArrayList<>();
        responseDtoList.add(responseDto);
        responseDtoList.add(responseDto2);
        when(reportService.getReports(anyLong())).thenReturn(responseDtoList);

        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/items/{itemId}/reports",itemId)
                .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(MockMvcRestDocumentationWrapper.document(
                        "get-reports",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("특정 매물에 대한 신고 다건 조회")
                                        .summary("특정 Item 신고 다건 조회")
                                        .tag("report")
                                        .requestHeaders(
                                                headerWithName("Authorization")
                                                        .description("Bearer (JWT 토큰)")
                                        )
                                        .pathParameters(
                                                parameterWithName("itemId").description("조회할 매물 ID")
                                        )
                                        .responseFields(
                                                fieldWithPath("message")
                                                        .description("성공 메시지 : Ok"),
                                                fieldWithPath("statusCode")
                                                        .description("성공 상태 코드 : 200"),
                                                fieldWithPath("data")
                                                        .description("본문 응답"),
                                                fieldWithPath("data[].id")
                                                        .description("신고 ID"),
                                                fieldWithPath("data[].itemId")
                                                        .description("신고된 매물 ID"),
                                                fieldWithPath("data[].reportingReason")
                                                        .description("신고된 사유"),
                                                fieldWithPath("data[].reportStatus")
                                                        .description("신고 상태 : 기본값 WAITING")
                                        )
                                        .responseSchema(Schema.schema("특정-매물-신고-다건-조회-성공-응답"))
                                        .build()
                        )));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", Matchers.hasSize(2)));
    }

    @Test
    @WithMockUser
    void updateReport() throws Exception {
        //given
        Long reportId = 1L;
        when(reportService.updateReport(anyLong(), any(ReportRequestDto.class), any(CustomUserDetails.class)))
                .thenReturn(responseDto);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(MockMvcRestDocumentationWrapper.document("update-report",
                        resource(ResourceSnippetParameters.builder()
                                .description("신고 ID와 수정할 신고사유를 받아서 신고를 수정합니다.")
                                .summary("매물 신고 수리")
                                .tag("report")
                                .pathParameters(
                                        parameterWithName("reportId")
                                                .description("수정할 신고 ID")
                                )
                                .requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Bearer (JWT 토큰)")
                                )
                                .requestFields(
                                        fieldWithPath("reportingReason")
                                                .description("신고 사유")
                                )
                                .responseFields(
                                        fieldWithPath("message")
                                                .description("성공 메시지 : Ok"),
                                        fieldWithPath("statusCode")
                                                .description("성공 상태 코드 : 200"),
                                        fieldWithPath("data")
                                                .description("본문 응답"),
                                        fieldWithPath("data.id")
                                                .description("신고 ID"),
                                        fieldWithPath("data.itemId")
                                                .description("수정된 신고 ID"),
                                        fieldWithPath("data.reportingReason")
                                                .description("수정된 신고 사유"),
                                        fieldWithPath("data.reportStatus")
                                                .description("신고 상태 : 기본값 WAITING")
                                )
                                .requestSchema(Schema.schema("신고-수정-성공-요청"))
                                .responseSchema(Schema.schema("신고-수정-성공-응답"))
                                .build()
                        )));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.reportingReason").value(reportingReason));
    }

    @Test
    @WithMockUser
    void deleteReport() throws Exception {
        //given
        Long reportId = 1L;
        doNothing().when(reportService).deleteReport(anyLong(),any(CustomUserDetails.class));

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.delete("/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer (JWT 토큰)"))
                .andDo(MockMvcRestDocumentationWrapper.document("update-report",
                        resource(ResourceSnippetParameters.builder()
                                .description("신고 ID를 받아서 해당 신고를 삭제합니다.")
                                .summary("신고 삭제")
                                .tag("report")
                                .pathParameters(
                                        parameterWithName("reportId")
                                                .description("수정할 신고 ID")
                                )
                                .requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Bearer (JWT 토큰)")
                                )
                                .responseFields(
                                        fieldWithPath("message")
                                                .description("성공 메시지 : Ok"),
                                        fieldWithPath("statusCode")
                                                .description("성공 상태 코드 : 200"),
                                        fieldWithPath("data")
                                                .description("따로 반환하는 본문 없습니다. : null")
                                )
                                .responseSchema(Schema.schema("신고-삭제-성공-응답"))
                                .build()
                        )));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }
}