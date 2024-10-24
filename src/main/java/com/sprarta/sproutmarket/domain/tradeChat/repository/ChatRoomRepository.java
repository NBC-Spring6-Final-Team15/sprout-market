package com.sprarta.sproutmarket.domain.tradeChat.repository;

import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.tradeChat.entity.ChatRoom;
import com.sprarta.sproutmarket.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByItemAndBuyer(Item item, User buyer);

    // 구매자 , 판매자로 연결된 채팅방 목록 조회
    @Query("SELECT c FROM ChatRoom c WHERE c.buyer.id = :userId OR c.seller.id = :userId")
    List<ChatRoom> findAllByUserId(Long userId);

}
