package com.sprarta.sproutmarket.domain.item.repository;

import com.sprarta.sproutmarket.domain.areas.dto.AdmNameDto;
import com.sprarta.sproutmarket.domain.category.entity.Category;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponseDto;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.id = :id AND i.seller.id = :sellerId")
    Optional<Item> findByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.category " +
        "JOIN FETCH i.seller " +
        "WHERE i.seller = :seller")
    Page<Item> findBySeller(Pageable pageable, @Param("seller") User seller);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.seller " +
        "JOIN FETCH i.category " +
        "WHERE i.category = :category")
    Page<Item> findByCategory(Pageable pageable, @Param("category") Category findCategory);

    @Query("SELECT i FROM Item i JOIN FETCH i.seller WHERE i.seller.address IN :areaList")
    Page<Item> findByAreaListAndUserArea(Pageable pageable, @Param("areaList") List<String> areaList);
}
