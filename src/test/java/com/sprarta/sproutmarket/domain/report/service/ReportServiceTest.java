package com.sprarta.sproutmarket.domain.report.service;

import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.repository.ItemRepository;
import com.sprarta.sproutmarket.domain.report.dto.ReportRequestDto;
import com.sprarta.sproutmarket.domain.report.dto.ReportResponseDto;
import com.sprarta.sproutmarket.domain.report.entity.Report;
import com.sprarta.sproutmarket.domain.report.enums.ReportStatus;
import com.sprarta.sproutmarket.domain.report.repository.ReportRepository;
import com.sprarta.sproutmarket.domain.report.service.ReportService;
import com.sprarta.sproutmarket.domain.review.dto.ReviewRequestDto;
import com.sprarta.sproutmarket.domain.review.dto.ReviewResponseDto;
import com.sprarta.sproutmarket.domain.review.entity.Review;
import com.sprarta.sproutmarket.domain.review.enums.ReviewRating;
import com.sprarta.sproutmarket.domain.review.repository.ReviewRepository;
import com.sprarta.sproutmarket.domain.review.service.ReviewService;
import com.sprarta.sproutmarket.domain.trade.repository.TradeRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void 신고_생성_정상동작() {
        // given
        Long itemId = 1L;

        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getId()).thenReturn(itemId);

        User user = Mockito.mock(User.class);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        ReportRequestDto dto = new ReportRequestDto("부적절한 물품");

        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));

        // when
        ReportResponseDto response = reportService.createReport(itemId, dto, customUserDetails);

        // then
        assertNotNull(response);
        assertEquals(itemId, response.getItemId());
        assertEquals("부적절한 물품", response.getReportingReason());

        verify(itemRepository).findById(itemId);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void 신고_조회_정상동작() {
        // given
        Long reportId = 1L;
        Long itemId = 2L;

        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getId()).thenReturn(itemId);

        Report report = Mockito.mock(Report.class);
        Mockito.when(report.getId()).thenReturn(reportId);
        Mockito.when(report.getItem()).thenReturn(item);
        Mockito.when(report.getReportingReason()).thenReturn("신고 이유");
        Mockito.when(report.getReportStatus()).thenReturn(ReportStatus.WAITING);

        when(reportRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(report));

        // when
        ReportResponseDto response = reportService.getReport(reportId);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(2L, response.getItemId());
        assertEquals("신고 이유", response.getReportingReason());
        assertEquals(ReportStatus.WAITING, response.getReportStatus());

    }


}
