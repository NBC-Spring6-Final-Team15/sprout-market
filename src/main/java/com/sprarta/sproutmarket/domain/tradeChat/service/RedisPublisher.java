package com.sprarta.sproutmarket.domain.tradeChat.service;

import com.sprarta.sproutmarket.domain.tradeChat.dto.TradeChatRequest;
import com.sprarta.sproutmarket.domain.tradeChat.entity.TradeChat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate; // Redis 상호작용에 사용되는 Spring 템플릿 클래스

    // 메시지를 Redis의 특정 채널(주제)로 발행
    public void publish(ChannelTopic topic, TradeChatRequest tradeChatRequest) {
        log.info("published topic = {}", topic.getTopic());
        redisTemplate.convertAndSend(topic.getTopic(), tradeChatRequest); // Redis 주제에 메시지 발행 redisTemplate.convertAndSend
    }

    // 채팅방 id 받아서 주제에 포함하는 방식 고려 ChannelTopic topic = new ChannelTopic("trade/" + chatRoomId.toString());
}
