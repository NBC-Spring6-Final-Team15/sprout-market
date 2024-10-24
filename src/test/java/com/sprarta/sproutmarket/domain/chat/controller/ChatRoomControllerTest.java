//package com.sprarta.sproutmarket.domain.chat.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sprarta.sproutmarket.domain.auth.controller.AuthController;
//import com.sprarta.sproutmarket.domain.tradeChat.controller.ChatRoomController;
//import com.sprarta.sproutmarket.domain.tradeChat.dto.ChatRoomDto;
//import com.sprarta.sproutmarket.domain.tradeChat.service.ChatRoomService;
//import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.RestDocumentationExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.when;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(ChatRoomController.class)
//@ExtendWith(RestDocumentationExtension.class)
//@MockBean(JpaMetamodelMappingContext.class)
//@AutoConfigureMockMvc(addFilters = false)
//public class ChatRoomControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ChatRoomService chatRoomService;
//
//    @InjectMocks
//    private ChatRoomController chatRoomController;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders
//                .standaloneSetup(chatRoomController)
//                .build();
//        objectMapper = new ObjectMapper();
//    }
//
//    @Test
//    void 채팅방_생성_성공() throws Exception {
//
//        ChatRoomDto chatRoomDto = new ChatRoomDto(
//                1L,
//                2L,
//                1L);
//
//        when(chatRoomService.createChatRoom(anyLong(), any(CustomUserDetails.class))).thenReturn(chatRoomDto);
//
//        // API 호출
//        mockMvc.perform(post("/items/{itemId}/chatrooms", 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(chatRoomDto))
//                        .principal(() -> "user")) // 인증 사용자 설정
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andDo(document("create-chat-room"));
//
//    }
//
//    @Test
//    void 채팅방_조회_성공() {
//
//    }
//
//    @Test
//    void 채팅방_목록_조회_성공() {
//
//    }
//
//    @Test
//    void 채팅방_삭제_성공() {
//
//    }
//
//}
