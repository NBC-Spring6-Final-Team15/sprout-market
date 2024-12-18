package com.sprarta.sproutmarket.domain.tradeChat.service;

import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.tradeChat.dto.TradeChatDto;
import com.sprarta.sproutmarket.domain.tradeChat.entity.ChatRoom;
import com.sprarta.sproutmarket.domain.tradeChat.entity.TradeChat;
import com.sprarta.sproutmarket.domain.tradeChat.repository.ChatRoomRepository;
import com.sprarta.sproutmarket.domain.tradeChat.repository.TradeChatRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final TradeChatRepository tradeChatRepository;
    private final RedisPublisher redisPublisher;

    public void saveChat(TradeChatDto tradeChatDto) {
        tradeChatRepository.save(new TradeChat(
                tradeChatDto.getSender(),
                tradeChatDto.getContent(),
                tradeChatDto.getRoomId()));
    }

    public List<TradeChatDto> getChats(Long roomId, CustomUserDetails authUser) {

        ChatRoom chatRoom = chatRoomRepository.findByIdOrElseThrow(roomId);

        if (!ObjectUtils.nullSafeEquals(chatRoom.getBuyer().getId(), authUser.getId())
                && !ObjectUtils.nullSafeEquals(chatRoom.getSeller().getId(), authUser.getId())) {
            throw new ApiException(ErrorStatus.FORBIDDEN_NOT_OWNED_CHATROOM);
        }

        List<TradeChatDto> tradeChatDtoList = new ArrayList<>();

        for (TradeChat tradeChat : tradeChatRepository.findAllByRoomId(roomId)) {
            TradeChatDto tradeChatDto = new TradeChatDto(
                    tradeChat.getRoomId(),
                    tradeChat.getSender(),
                    tradeChat.getContent(),
                    tradeChat.getReadCount()
            );
            tradeChatDtoList.add(tradeChatDto);
        }

        return tradeChatDtoList;
    }

    // 채팅방 id 소속 채팅의 카운트 감소 , 사용자의 id를 받아 작성자와 일치하지 않을 경우, 0보다 클 경우 감소
    @Transactional
    public void decreaseReadCount(Long roomId, String sender) {
        for (TradeChat tradeChat : tradeChatRepository.findAllByRoomId(roomId)) {
            if (tradeChat.getReadCount() > 0 && !ObjectUtils.nullSafeEquals(tradeChat.getSender(),
                    sender.replaceAll("\"", ""))) {
                tradeChat.decreaseReadCount();
            }
        }
    }

    public void chatRoomMatch(Long roomId, Long userId) {
        if (!ObjectUtils.nullSafeEquals(chatRoomRepository.findByIdOrElseThrow(roomId).getSeller().getId(), userId)
                && !ObjectUtils.nullSafeEquals(chatRoomRepository.findByIdOrElseThrow(roomId).getBuyer().getId(), userId)) {
            throw new ApiException(ErrorStatus.FORBIDDEN_NOT_OWNED_CHATROOM);
        }
    }

    public void publishChat(Long roomId, TradeChatDto tradeChatDto) {
        redisPublisher.publish(roomId, tradeChatDto);
    }

}
