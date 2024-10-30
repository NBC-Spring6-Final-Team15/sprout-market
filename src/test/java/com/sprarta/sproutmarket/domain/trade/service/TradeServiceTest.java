package com.sprarta.sproutmarket.domain.trade.service;

import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.entity.ItemSaleStatus;
import com.sprarta.sproutmarket.domain.trade.dto.TradeResponseDto;
import com.sprarta.sproutmarket.domain.trade.entity.Trade;
import com.sprarta.sproutmarket.domain.trade.enums.TradeStatus;
import com.sprarta.sproutmarket.domain.trade.repository.TradeRepository;
import com.sprarta.sproutmarket.domain.tradeChat.entity.ChatRoom;
import com.sprarta.sproutmarket.domain.tradeChat.repository.ChatRoomRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {
    @InjectMocks
    private TradeService tradeService;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private ChatRoom chatRoom;
    private CustomUserDetails sellerUserDetails;
    private Item item;
    private Trade trade;

    @BeforeEach
    void setUp() {
        User buyer = new User();
        ReflectionTestUtils.setField(buyer,"id",1L);
        ReflectionTestUtils.setField(buyer,"nickname","buyer");

        User seller = new User();
        ReflectionTestUtils.setField(seller,"id",2L);
        ReflectionTestUtils.setField(seller,"nickname","seller");

        item = new Item();
        ReflectionTestUtils.setField(item,"id",1L);
        ReflectionTestUtils.setField(item,"title","아이템");
        ReflectionTestUtils.setField(item,"itemSaleStatus", ItemSaleStatus.WAITING);
        sellerUserDetails = new CustomUserDetails(seller);

        chatRoom = new ChatRoom(buyer, seller,item);
        ReflectionTestUtils.setField(chatRoom,"id",1L);

        trade = new Trade(chatRoom);
        ReflectionTestUtils.setField(trade,"id",1L);
    }

    @Nested
    class 거래_생성 {

        @Test
        void 거래_생성_성공() {
            when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
            when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
            doNothing().when(simpMessagingTemplate).convertAndSend(anyString(),anyString());

            TradeResponseDto responseDto = tradeService.reserveTrade(1L, sellerUserDetails);

            assertThat(item.getItemSaleStatus()).isEqualTo(ItemSaleStatus.RESERVED);
            assertThat(responseDto.getItemTitle()).isEqualTo("아이템");
            assertThat(responseDto.getSellerName()).isEqualTo("seller");
            assertThat(responseDto.getBuyerName()).isEqualTo("buyer");
        }

        @Test
        void 거래_생성_실패__판매자와_생성_요청자가_다른_경우_예외_발생() {
            User anyone = new User();
            ReflectionTestUtils.setField(anyone,"id",3L);
            CustomUserDetails anyoneDetails = new CustomUserDetails(anyone);

            when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));

            assertThatThrownBy(() -> tradeService.reserveTrade(1L, anyoneDetails))
                    .isInstanceOf(ApiException.class);

        }

        @Test
        void 거래_생성_실패__예약중인_아이템이_대기중이_아닌_경우_예외_발생() {
            ReflectionTestUtils.setField(item,"itemSaleStatus",ItemSaleStatus.RESERVED);

            when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));

            assertThatThrownBy(() -> tradeService.reserveTrade(1L, sellerUserDetails))
                    .isInstanceOf(ApiException.class);
        }
    }

    @Test
    void 거래_상태_변경_성공() {
        when(tradeRepository.findById(1L)).thenReturn(Optional.of(trade));
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(),anyString());

        tradeService.finishTrade(1L, sellerUserDetails);

        assertThat(trade.getTradeStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(trade.getChatRoom().getItem().getItemSaleStatus()).isEqualTo(ItemSaleStatus.SOLD);
    }
}