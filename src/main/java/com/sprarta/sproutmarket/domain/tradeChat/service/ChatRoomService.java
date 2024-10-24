package com.sprarta.sproutmarket.domain.tradeChat.service;

import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.repository.ItemRepository;
import com.sprarta.sproutmarket.domain.tradeChat.dto.ChatRoomDto;
import com.sprarta.sproutmarket.domain.tradeChat.entity.ChatRoom;
import com.sprarta.sproutmarket.domain.tradeChat.repository.ChatRoomRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    // 채팅 저장 , 방식 결정에 따라 이후 수정 예정

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // 채팅방 생성 - 구매자와 상품 사이에는 채팅방이 하나만 존재해야 함, 기본적으로 방 생성은 구매자만 가능?
    @Transactional
    public ChatRoomDto createChatRoom(Long itemId, CustomUserDetails authUser) {
        User buyer = findUserById(authUser.getId());
        Item item = findItemById(itemId);

        // 상품과 구매자 사이에 기존 채팅방이 있는지 확인
        Optional<ChatRoom> findChatRoom = chatRoomRepository.findByItemAndBuyer(item, buyer);
        if (findChatRoom.isPresent()) {
            throw new ApiException(ErrorStatus.CONFLICT_CHATROOM);
        }

        // 상품 판매자와 현재 사용자 id 가 동일할 경우 생성 X
        if (ObjectUtils.nullSafeEquals(item.getSeller().getId(), buyer.getId())) {
            throw new ApiException(ErrorStatus.FORBIDDEN_CHATROOM_CREATE);
        }

        // 새로운 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .buyer(buyer)
                .seller(item.getSeller())
                .item(item)
                .build();

        ChatRoom saveChatRoom = chatRoomRepository.save(chatRoom);

        return new ChatRoomDto(
                saveChatRoom.getBuyer(),
                saveChatRoom.getSeller(),
                saveChatRoom.getItem()
        );
    }

    // 채팅방 조회 - 이후 채팅 내역을 어떻게 저장하나에 따라 조회 부분 변경 필요
    public ChatRoomDto getChatRoom(Long chatRoomId, CustomUserDetails authUser) {
        User user = findUserById(authUser.getId());
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        chatRoomMatch(chatRoom, user.getId());

        return new ChatRoomDto(
                chatRoom.getBuyer(),
                chatRoom.getSeller(),
                chatRoom.getItem()
        );
    }

    // 채팅방 삭제 - 구매자, 판매자가 나갈 경우 삭제
    @Transactional
    public void deleteChatRoom(Long chatRoomId, CustomUserDetails authUser) {
        User user = findUserById(authUser.getId());
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        chatRoomMatch(chatRoom, user.getId());

        chatRoomRepository.deleteById(chatRoomId);
    }

    // 사용자 소속 채팅방 전체 조회 - 판매자는 상품 기준 채팅방 추가?
    public List<ChatRoomDto> getAllChatRooms(CustomUserDetails authUser) {
        User user = findUserById(authUser.getId());

        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByUserId(user.getId());

        List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomList) {
            ChatRoomDto chatRoomDto = new ChatRoomDto(
                    chatRoom.getBuyer(),
                    chatRoom.getSeller(),
                    chatRoom.getItem()
            );
            chatRoomDtoList.add(chatRoomDto);
        }

        return chatRoomDtoList;
    }

    // 사용자 존재 확인
    private User findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        return user;
    }

    // 상품 존재 확인
    private Item findItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_ITEM));

        return item;
    }

    // 채팅방 존재 확인
    private ChatRoom findChatRoomById(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM));

        return chatRoom;
    }

    // 채팅방에 소속된 사용자(구매자 , 판매자) id 와 현재 사용자 id 일치 여부 확인
    private void chatRoomMatch(ChatRoom chatRoom, Long userId) {
        if (!ObjectUtils.nullSafeEquals(chatRoom.getBuyer().getId(), userId)
        && !ObjectUtils.nullSafeEquals(chatRoom.getSeller().getId(), userId)) {
            throw new ApiException(ErrorStatus.FORBIDDEN_NOT_OWNED_CHATROOM);
        }
    }

}
