package com.sprarta.sproutmarket.domain.tradeChat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.domain.tradeChat.dto.TradeChatRequest;
import com.sprarta.sproutmarket.domain.tradeChat.dto.TradeChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper; // JSON과 Java 객체 변환 지원
    private final RedisTemplate<String, Object> redisTemplate; // Redis 상호작용에 사용되는 Spring 템플릿 클래스
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {

            // Redis에서 수신한 메시지의 본문 가져와 수신한 메시지를 문자열로 변환
            String publishMessage = (String) redisTemplate
                    .getStringSerializer().deserialize(message.getBody());

            // 문자열 형식의 메시지를 TradeChatRequest 객체로 변환
            TradeChatRequest tradeChatRequest = objectMapper.readValue(
                    publishMessage, TradeChatRequest.class);

            TradeChatResponse tradeChatResponse = new TradeChatResponse(
                    tradeChatRequest.getContent(),
                    tradeChatRequest.getChatReadStatus()
            );

            // WebSocket을 통해 특정 주제에 tradeChatResponse 전송
            messagingTemplate.convertAndSend(
                    "/sub/trade/" + tradeChatRequest.getChatRoom().getId() , // 세부 경로 지정을 메시지 전송할 때 하는 것
                    tradeChatResponse);
            // 이후 다중 주제 지원하도록 수정? 일단 trade로

        } catch (Exception e) {
            log.error(e.getMessage()); // 이후 오류 변경 필요
        }

        log.info("받은 메시지 : {}", message.toString());
    }

}
