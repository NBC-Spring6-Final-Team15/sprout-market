package com.sprarta.sproutmarket.config;

import com.sprarta.sproutmarket.domain.tradeChat.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> chatRedisTemplate = new RedisTemplate<>(); // RedisTemplate 생성 키 String 타입 값 Object 타입

        chatRedisTemplate.setConnectionFactory(connectionFactory); // Redis 서버와 연결 관리

        chatRedisTemplate.setKeySerializer(new StringRedisSerializer()); // 키를 String 형식으로 직렬화
        chatRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 값 데이터 JSON 형식으로 직렬화

        return chatRedisTemplate;
    }

    // redis pub/sub 메세지를 처리하는 listener 설정
    @Bean
    public RedisMessageListenerContainer redisMessageListener(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer(); // Redis에서 발행된 메시지 처리할 리스너 관리

        container.setConnectionFactory(connectionFactory); // Redis 서버로부터 발행된 메시지 수신
        container.addMessageListener(listenerAdapter, channelTopic); // 수신할 메시지에 대한 리스너 추가

        return container;
    }

    // Redis 메시지를 수신하고 처리하는 데 필요한 어댑터
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    // Redis의 pub/sub 기능에서 사용할 주제 정의 이후 중고 거래 외에도 채팅 사용시 유용?
    @Bean
    public ChannelTopic channelTopic() { // Topic 공유를 위해 단일화
        return new ChannelTopic("trade");
    }

}
