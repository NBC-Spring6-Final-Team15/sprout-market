package com.sprarta.sproutmarket.domain.tradeChat.controller;

import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.tradeChat.dto.ChatRoomDto;
import com.sprarta.sproutmarket.domain.tradeChat.service.ChatRoomService;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 채팅방 생성
    @PostMapping("/items/{itemId}/chatrooms")
    public ResponseEntity<ApiResponse<ChatRoomDto>> createChatRoom(
            @PathVariable("itemId") Long itemId, @AuthenticationPrincipal CustomUserDetails authUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.createSuccess("Created",201, chatRoomService.createChatRoom(itemId, authUser)));
    }

    // 채팅방 조회
    @GetMapping("/chatrooms/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getChatRoom(
            @PathVariable("chatRoomId") Long chatRoomId, @AuthenticationPrincipal CustomUserDetails authUser) {
        return ResponseEntity.ok
                (ApiResponse.onSuccess(chatRoomService.getChatRoom(chatRoomId, authUser)));
    }

    // 사용자의 채팅방 목록 조회
    @GetMapping("/chatrooms")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getAllChatRooms(
            @AuthenticationPrincipal CustomUserDetails authUser) {
        return ResponseEntity.ok
                (ApiResponse.onSuccess(chatRoomService.getChatRooms(authUser)));
    }

    // 채팅방 삭제
    @DeleteMapping("/chatrooms/{chatRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatRoom(
            @PathVariable("chatRoomId") Long chatRoomId, @AuthenticationPrincipal CustomUserDetails authUser) {
        chatRoomService.deleteChatRoom(chatRoomId, authUser);
        return ResponseEntity.ok
                (ApiResponse.onSuccess(null));
    }

}
