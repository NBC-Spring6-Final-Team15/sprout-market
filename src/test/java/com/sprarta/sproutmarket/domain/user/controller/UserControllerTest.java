package com.sprarta.sproutmarket.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.sprarta.sproutmarket.domain.CommonMockMvcControllerTestSetUp;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.user.dto.request.UserChangePasswordRequest;
import com.sprarta.sproutmarket.domain.user.dto.request.UserDeleteRequest;
import com.sprarta.sproutmarket.domain.user.dto.response.UserAdminResponse;
import com.sprarta.sproutmarket.domain.user.dto.response.UserResponse;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.sprarta.sproutmarket.domain.common.entity.Status.ACTIVE;
import static com.sprarta.sproutmarket.domain.common.entity.Status.DELETED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends CommonMockMvcControllerTestSetUp {
    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        User mockUser = new User("username", "email@example.com", "encodedOldPassword", "nickname", "010-1234-5678", "address", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        CustomUserDetails mockAuthUser = new CustomUserDetails(mockUser);

        // Set the authenticated user in the SecurityContext
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mockAuthUser, null, mockAuthUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.getUser(anyLong())).thenReturn(new UserResponse(mockUser.getId(), mockUser.getEmail()));
        doNothing().when(userService).changePassword(any(CustomUserDetails.class), any(UserChangePasswordRequest.class));
        doNothing().when(userService).deleteUser(any(CustomUserDetails.class), any(UserDeleteRequest.class));
    }

    @Test
    @WithMockUser
    void getUserSuccess() throws Exception {
        // given
        UserResponse userResponse = new UserResponse(1L, "email@example.com");
        given(userService.getUser(anyLong())).willReturn(userResponse);

        // when, then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-user",
                        resource(ResourceSnippetParameters.builder()
                                .description("사용자 조회 API")
                                .summary("특정 사용자 정보를 조회합니다.")
                                .tag("User")
                                .pathParameters(
                                        parameterWithName("userId").description("사용자 ID") // URL 템플릿 설명 추가
                                )
                                .responseFields(
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드"),
                                        fieldWithPath("data.id").description("유저 ID"),
                                        fieldWithPath("data.email").description("유저 이메일")
                                )
                                .responseSchema(Schema.schema("유저-조회-성공-응답"))
                                .build())
                ));
    }

    @Test
    @WithMockUser
    void getUserFail_UserNotFound() throws Exception {
        // given
        given(userService.getUser(anyLong())).willThrow(new ApiException(ErrorStatus.NOT_FOUND_USER));

        // when, then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/users/{userId}", 999L) // 존재하지 않는 유저 ID
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found 기대
                .andDo(document("get-user-fail-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .description("사용자 조회 실패 API")
                                .summary("존재하지 않는 사용자 ID로 조회할 때")
                                .tag("User")
                                .responseFields(List.of(
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                ))
                                .responseSchema(Schema.schema("유저-조회-실패-응답"))
                                .build())
                ));
    }


    @Test
    @WithMockUser
    void changePasswordSuccess() throws Exception {
        // given
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword", "NewPass1!"); // 패턴에 맞는 비밀번호로 수정

        // when, then
        mockMvc.perform(RestDocumentationRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"oldPassword\",\"newPassword\":\"NewPass1!\"}")
                        .principal(() -> "email@example.com"))
                .andExpect(status().isOk())
                .andDo(document("change-password",
                        resource(ResourceSnippetParameters.builder()
                                .description("비밀번호 변경 API")
                                .summary("사용자의 비밀번호를 변경합니다.")
                                .tag("User")
                                .requestFields(List.of(
                                        fieldWithPath("oldPassword").description("사용자의 기존 비밀번호"),
                                        fieldWithPath("newPassword").description("변경할 새로운 비밀번호")
                                ))
                                .responseFields(List.of(
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                ))
                                .requestSchema(Schema.schema("비밀번호-변경-성공-요청"))
                                .responseSchema(Schema.schema("비밀번호-변경-성공-응답"))
                                .build())
                ));
    }

    @Test
    @WithMockUser
    void changePasswordFail_IncorrectOldPassword() throws Exception {
        // given
        doThrow(new ApiException(ErrorStatus.BAD_REQUEST_PASSWORD))
                .when(userService).changePassword(any(CustomUserDetails.class), any(UserChangePasswordRequest.class));

        // when, then
        mockMvc.perform(RestDocumentationRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"wrongOldPassword\",\"newPassword\":\"NewPass1!\"}"))
                .andExpect(status().isBadRequest()) // 400 Bad Request 기대
                .andDo(document("change-password-fail-wrong-old-password",
                        resource(ResourceSnippetParameters.builder()
                                .description("비밀번호 변경 실패 API")
                                .summary("잘못된 기존 비밀번호로 인한 비밀번호 변경 실패")
                                .tag("User")
                                .responseFields(List.of(
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                ))
                                .responseSchema(Schema.schema("비밀번호-변경-실패-응답"))
                                .build())
                ));
    }

    @Test
    @WithMockUser
    void deleteUserSuccess() throws Exception {
        // given

        // when, then
        mockMvc.perform(delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andDo(document("delete-user",
                        resource(ResourceSnippetParameters.builder()
                                .description("사용자 삭제 API")
                                .summary("사용자를 삭제합니다.")
                                .tag("User")
                                .requestFields(List.of(
                                        fieldWithPath("password").description("사용자의 비밀번호")
                                ))
                                .responseFields(List.of(
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                ))
                                .requestSchema(Schema.schema("유저-삭제-성공-요청"))
                                .responseSchema(Schema.schema("유저-삭제-성공-응답"))
                                .build())
                ));
    }

    @Test
    @WithMockUser
    void deleteUserFail_IncorrectPassword() throws Exception {
        // given
        UserDeleteRequest deleteRequest = new UserDeleteRequest("wrongPassword");
        doThrow(new ApiException(ErrorStatus.BAD_REQUEST_PASSWORD))
                .when(userService).deleteUser(any(CustomUserDetails.class), any(UserDeleteRequest.class));

        // when, then
        mockMvc.perform(delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrongPassword\"}"))
                .andExpect(status().isBadRequest()) // 400 Bad Request 기대
                .andDo(document("delete-user-fail-wrong-password",
                        resource(ResourceSnippetParameters.builder()
                                .description("사용자 삭제 실패 API")
                                .summary("잘못된 비밀번호로 인한 사용자 삭제 실패")
                                .tag("User")
                                .responseFields(List.of(
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                ))
                                .responseSchema(Schema.schema("유저-삭제-실패-응답"))
                                .build())
                ));
    }

    @Test
    @WithMockUser
    void updateUserAddressSuccess() throws Exception {
        // given
        String jsonBody = "{ \"longitude\": 126.9780, \"latitude\": 37.5665 }";

        // when, then
        mockMvc.perform(patch("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)) // JSON body로 데이터 전달
                .andExpect(status().isOk())
                .andDo(document("update-user-address",
                        resource(ResourceSnippetParameters.builder()
                                .description("사용자 주소 업데이트 API")
                                .summary("사용자의 주소를 업데이트합니다.")
                                .tag("User")
                                .requestFields(List.of(
                                        fieldWithPath("longitude").description("사용자 주소의 경도"),
                                        fieldWithPath("latitude").description("사용자 주소의 위도")
                                ))
                                .responseFields(List.of(
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                ))
                                .requestSchema(Schema.schema("주소-변경-성공-요청"))
                                .responseSchema(Schema.schema("주소-변경-성공-응답"))
                                .build())
                ));
    }

    @Test
    @WithMockUser
    void activateUser_Success() throws Exception {
        // given
        doNothing().when(userService).activateUser(anyLong());

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/users/admin/deleted/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("activate-user",
                        resource(ResourceSnippetParameters.builder()
                                .description("탈퇴 유저 복원 API")
                                .summary("특정 유저를 복원합니다.")
                                .tag("User")
                                .pathParameters(RequestDocumentation.parameterWithName("userId").description("유저 ID"))
                                .responseFields(
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드")
                                )
                                .responseSchema(Schema.schema("유저-복원-성공-응답"))
                                .build()
                        )));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        // given
        Page<UserAdminResponse> users = new PageImpl<>(List.of(
                new UserAdminResponse(1L, "username1", "email@example.com", ACTIVE),
                new UserAdminResponse(2L, "username2", "another@example.com", DELETED)
        ));

        given(userService.getAllUsers(any(Pageable.class))).willReturn(users);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(document("get-all-users",
                        resource(ResourceSnippetParameters.builder()
                                .description("모든 유저 조회 API")
                                .summary("모든 유저 목록을 조회합니다.")
                                .tag("User")
                                .responseFields(
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("statusCode").description("응답 상태 코드"),
                                        fieldWithPath("data.content[].userId").description("유저 ID"),
                                        fieldWithPath("data.content[].username").description("유저 이름"),
                                        fieldWithPath("data.content[].email").description("유저 이메일"),
                                        fieldWithPath("data.content[].status").description("유저 상태"),
                                        fieldWithPath("data.pageable").description("페이징 정보"),
                                        fieldWithPath("data.totalPages").description("총 페이지 수"),
                                        fieldWithPath("data.totalElements").description("총 유저 수"),
                                        fieldWithPath("data.last").description("마지막 페이지 여부"),
                                        fieldWithPath("data.size").description("페이지 크기"),
                                        fieldWithPath("data.number").description("현재 페이지 번호"),
                                        fieldWithPath("data.sort.empty").description("정렬 정보가 비어있는지 여부"),
                                        fieldWithPath("data.sort.sorted").description("정렬되었는지 여부"),
                                        fieldWithPath("data.sort.unsorted").description("정렬되지 않았는지 여부"),
                                        fieldWithPath("data.first").description("첫 페이지 여부"),
                                        fieldWithPath("data.numberOfElements").description("현재 페이지의 요소 수"),
                                        fieldWithPath("data.empty").description("페이지가 비어있는지 여부")
                                )
                                .responseSchema(Schema.schema("모든-유저-조회-응답"))
                                .build()
                        )));
    }

}
