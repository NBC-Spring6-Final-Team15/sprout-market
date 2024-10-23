package com.sprarta.sproutmarket.domain.report.controller;


import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.service.ItemService;
import com.sprarta.sproutmarket.domain.report.dto.ReportRequestDto;
import com.sprarta.sproutmarket.domain.report.dto.ReportResponseDto;
import com.sprarta.sproutmarket.domain.report.service.ReportService;
import com.sprarta.sproutmarket.domain.review.dto.ReviewRequestDto;
import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReportController {

    private final ReportService reportService;
    private final ItemService itemService;

    // 생성
    @PostMapping("/reports/{itemId}")
    public ResponseEntity<ApiResponse<ReportResponseDto>> createReport(
            @PathVariable Long itemId,
            @RequestBody @Valid ReportRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        ReportResponseDto responseDto = reportService.createReport(itemId, dto, customUserDetails);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 신고 단건 조회
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponseDto>> getReport(
            @PathVariable Long reportId
    ) {
        ReportResponseDto responseDto = reportService.getReport(reportId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // item 신고 전체 조회
    @GetMapping("/items/{itemId}/reports")
    public ResponseEntity<ApiResponse<List<ReportResponseDto>>> getReports(
            @PathVariable Long itemId
    ) {
        List<ReportResponseDto> responseDto = reportService.getReports(itemId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 수정
    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponseDto>> updateReport(
            @PathVariable Long reportId,
            @RequestBody @Valid ReportRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        ReportResponseDto responseDto = reportService.updateReport(reportId, dto, customUserDetails);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    // 삭제
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        reportService.deleteReport(reportId, customUserDetails);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }



}
