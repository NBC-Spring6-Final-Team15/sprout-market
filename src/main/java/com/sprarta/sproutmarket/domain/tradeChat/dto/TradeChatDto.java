package com.sprarta.sproutmarket.domain.tradeChat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class TradeChatDto {

    private Long roomId;
    private String sender;
    private String content;

    public TradeChatDto(Long roomId, String sender, String content) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
    }

}