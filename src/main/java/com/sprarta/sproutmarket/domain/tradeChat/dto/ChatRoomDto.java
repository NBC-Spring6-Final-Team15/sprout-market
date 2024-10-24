package com.sprarta.sproutmarket.domain.tradeChat.dto;

import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.user.entity.User;
import lombok.Getter;

@Getter
public class ChatRoomDto {

    private User buyer;
    private User seller;
    private Item item;

    public ChatRoomDto(User buyer, User seller, Item item) {
        this.buyer = buyer;
        this.seller = seller;
        this.item = item;
    }

}
