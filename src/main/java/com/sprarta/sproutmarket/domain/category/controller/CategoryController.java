package com.sprarta.sproutmarket.domain.category.controller;

import com.sprarta.sproutmarket.domain.category.dto.CategoryResponseDto;
import com.sprarta.sproutmarket.domain.category.service.CategoryService;
import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getCategoryItems(){
        List<CategoryResponseDto> itemResponseDto = categoryService.getCategories();
        return ResponseEntity.ok(ApiResponse.onSuccess(itemResponseDto));
    }
}
