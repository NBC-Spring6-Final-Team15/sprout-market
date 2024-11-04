package com.sprarta.sproutmarket.domain.image.itemImage.repository;

import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.image.itemImage.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    void deleteByName(String imageAddress);

    @Query("SELECT i FROM ItemImage i WHERE i.name = :imageAddress")
    Optional<ItemImage> findByName(@Param("imageAddress") String imageAddress);

    default ItemImage findByNameOrElseThrow(String imageAddress) {
        return findByName(imageAddress)
            .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_IMAGE));
    }

    default ItemImage findByIdOrElseThrow(Long imageId) {
        return findById(imageId)
            .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_IMAGE));
    }
}