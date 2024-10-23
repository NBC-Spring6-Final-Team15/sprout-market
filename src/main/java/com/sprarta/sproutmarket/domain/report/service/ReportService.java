package com.sprarta.sproutmarket.domain.report.service;


import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.repository.ItemRepository;
import com.sprarta.sproutmarket.domain.report.dto.ReportRequestDto;
import com.sprarta.sproutmarket.domain.report.dto.ReportResponseDto;
import com.sprarta.sproutmarket.domain.report.entity.Report;
import com.sprarta.sproutmarket.domain.report.repository.ReportRepository;
import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
import com.sprarta.sproutmarket.domain.review.entity.Review;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ItemRepository itemRepository;

    public ReportResponseDto createReport(Long itemId, ReportRequestDto dto, CustomUserDetails customUserDetails) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ApiException(ErrorStatus.NOT_FOUND_ITEM));
        User user = User.fromAuthUser(customUserDetails);

        Report report = new Report(
                dto.getReportingReason(),
                user,
                item
        );
        reportRepository.save(report);

        return new ReportResponseDto(
                report.getId(),
                report.getItem().getId(),
                report.getReportingReason(),
                report.getReportStatus()
        );

    }

    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() ->
                new ApiException(ErrorStatus.NOT_FOUND_REPORT));

        return new ReportResponseDto(
                report.getId(),
                report.getItem().getId(),
                report.getReportingReason(),
                report.getReportStatus()
        );
    }


    public List<ReportResponseDto> getReports(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ApiException(ErrorStatus.NOT_FOUND_ITEM));

        List<Report> reports = reportRepository.findByItemId(item.getId());

        List<ReportResponseDto> responseDtos = new ArrayList<>();

        for (Report report : reports) {
            ReportResponseDto responseDto = new ReportResponseDto(
                    report.getId(),
                    report.getItem().getId(),
                    report.getReportingReason(),
                    report.getReportStatus()
            );
            responseDtos.add(responseDto);
        }
        return responseDtos;
    }

    @Transactional
    public ReportResponseDto updateReport(Long reportId, ReportRequestDto dto, CustomUserDetails customUserDetails) {
        Report report = reportRepository.findById(reportId).orElseThrow(() ->
                new ApiException(ErrorStatus.NOT_FOUND_REPORT));

        if (!report.getUser().getId().equals(customUserDetails.getId())) {
            throw new ApiException(ErrorStatus.FORBIDDEN_REPORT_UPDATE);
        }

        report.update(
                dto.getReportingReason()
        );
        reportRepository.save(report);
        return new ReportResponseDto(
                report.getId(),
                report.getItem().getId(),
                report.getReportingReason(),
                report.getReportStatus()
        );
    }

    @Transactional
    public void deleteReport(Long reportId, CustomUserDetails customUserDetails) {
        Report report = reportRepository.findById(reportId).orElseThrow(() ->
                new ApiException(ErrorStatus.NOT_FOUND_REPORT));

        if (!report.getUser().getId().equals(customUserDetails.getId())) {
            throw new ApiException(ErrorStatus.FORBIDDEN_REPORT_DELETE);
        }

        reportRepository.delete(report);
    }

}
