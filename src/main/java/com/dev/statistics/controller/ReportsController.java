package com.dev.statistics.controller;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import com.dev.export.model.ExportFormat;
import com.dev.export.service.ExportService;
import com.dev.penalty.model.Penalty;
import com.dev.penalty.model.PenaltyStatus;
import com.dev.penalty.repository.PenaltyRepository;
import com.dev.statistics.dto.BookStatisticsResponse;
import com.dev.statistics.dto.DashboardMetricsResponse;
import com.dev.statistics.dto.OverdueReportResponse;
import com.dev.statistics.dto.PenaltyReportResponse;
import com.dev.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Thống kê & Báo cáo", description = "API thống kê và báo cáo - dashboard, lịch sử mượn, sách quá hạn, phạt và sách phổ biến")
public class ReportsController {

    private final StatisticsService statisticsService;
    private final BorrowRepository borrowRepository;
    private final PenaltyRepository penaltyRepository;
    private final ExportService exportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboard() {
        DashboardMetricsResponse metrics = statisticsService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, metrics));
    }

    @GetMapping("/borrow-history")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> getBorrowHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("End date must be after or equal to start date")
            );
        }
        
        if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Date range cannot exceed 1 year")
            );
        }
        
        List<Borrow> borrows = borrowRepository.findByBorrowDateBetweenWithDetails(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, borrows));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<OverdueReportResponse>>> getOverdueReport() {
        List<Borrow> overdueBorrows = borrowRepository.findByStatusAndDueDateBeforeWithDetails(
                BorrowStatus.OVERDUE, LocalDate.now().plusDays(1));
        
        List<OverdueReportResponse> response = overdueBorrows.stream()
                .map(borrow -> {
                    long daysOverdue = ChronoUnit.DAYS.between(borrow.getDueDate(), LocalDate.now());
                    return OverdueReportResponse.builder()
                            .borrowId(borrow.getId())
                            .readerId(borrow.getUser().getId())
                            .readerName(borrow.getUser().getFullName())
                            .readerEmail(borrow.getUser().getEmail())
                            .bookCopyId(borrow.getBookCopy().getId())
                            .bookTitle(borrow.getBookCopy().getBook().getTitle())
                            .isbn(borrow.getBookCopy().getBook().getIsbn())
                            .borrowDate(borrow.getBorrowDate())
                            .dueDate(borrow.getDueDate())
                            .daysOverdue((int) daysOverdue)
                            .build();
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/penalties")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> getPenaltyReport(
            @RequestParam(required = false) String status) {
        
        List<Penalty> penalties;
        if (status != null) {
            PenaltyStatus penaltyStatus;
            try {
                penaltyStatus = PenaltyStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid status. Valid values: UNPAID, PAID, WAIVED")
                );
            }
            penalties = penaltyRepository.findByStatusWithDetails(penaltyStatus);
        } else {
            penalties = penaltyRepository.findAllWithDetails();
        }
        
        List<PenaltyReportResponse> response = penalties.stream()
                .map(penalty -> PenaltyReportResponse.builder()
                        .penaltyId(penalty.getPenaltyId())
                        .penaltyType(penalty.getType().name())
                        .amount(penalty.getAmount())
                        .status(penalty.getStatus().name())
                        .readerId(penalty.getReader().getId())
                        .readerName(penalty.getReader().getFullName())
                        .createdDate(penalty.getCreatedDate())
                        .paidDate(penalty.getPaidDate())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/popular-books")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookStatisticsResponse>>> getPopularBooks(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<BookStatisticsResponse> books = statisticsService.getTopBorrowedBooks(limit);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, books));
    }

    @GetMapping("/borrow-history/export")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Export borrow history", description = "Xuất lịch sử mượn sách ra file CSV hoặc Excel")
    public ResponseEntity<?> exportBorrowHistory(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("End date must be after or equal to start date"));
        }
        
        if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Date range cannot exceed 1 year"));
        }
        
        List<Borrow> borrows = borrowRepository.findByBorrowDateBetweenWithDetails(startDate, endDate);
        byte[] data = exportService.exportBorrowHistory(borrows, format);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportService.getFileName("borrow-history", format) + "\"")
                .contentType(MediaType.parseMediaType(exportService.getContentType(format)))
                .body(data);
    }

    @GetMapping("/overdue/export")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Export overdue report", description = "Xuất báo cáo sách quá hạn ra file CSV hoặc Excel")
    public ResponseEntity<?> exportOverdueReport(
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        
        List<Borrow> overdueBorrows = borrowRepository.findByStatusAndDueDateBeforeWithDetails(
                BorrowStatus.OVERDUE, LocalDate.now().plusDays(1));
        
        List<OverdueReportResponse> response = overdueBorrows.stream()
                .map(borrow -> {
                    long daysOverdue = ChronoUnit.DAYS.between(borrow.getDueDate(), LocalDate.now());
                    return OverdueReportResponse.builder()
                            .borrowId(borrow.getId())
                            .readerId(borrow.getUser().getId())
                            .readerName(borrow.getUser().getFullName())
                            .readerEmail(borrow.getUser().getEmail())
                            .bookCopyId(borrow.getBookCopy().getId())
                            .bookTitle(borrow.getBookCopy().getBook().getTitle())
                            .isbn(borrow.getBookCopy().getBook().getIsbn())
                            .borrowDate(borrow.getBorrowDate())
                            .dueDate(borrow.getDueDate())
                            .daysOverdue((int) daysOverdue)
                            .build();
                })
                .collect(Collectors.toList());
        
        byte[] data = exportService.exportOverdueReport(response, format);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportService.getFileName("overdue-report", format) + "\"")
                .contentType(MediaType.parseMediaType(exportService.getContentType(format)))
                .body(data);
    }

    @GetMapping("/penalties/export")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Export penalty report", description = "Xuất báo cáo phạt ra file CSV hoặc Excel")
    public ResponseEntity<?> exportPenaltyReport(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) String status) {
        
        List<Penalty> penalties;
        if (status != null) {
            PenaltyStatus penaltyStatus;
            try {
                penaltyStatus = PenaltyStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid status. Valid values: UNPAID, PAID, WAIVED")
                );
            }
            penalties = penaltyRepository.findByStatusWithDetails(penaltyStatus);
        } else {
            penalties = penaltyRepository.findAllWithDetails();
        }
        
        List<PenaltyReportResponse> response = penalties.stream()
                .map(penalty -> PenaltyReportResponse.builder()
                        .penaltyId(penalty.getPenaltyId())
                        .penaltyType(penalty.getType().name())
                        .amount(penalty.getAmount())
                        .status(penalty.getStatus().name())
                        .readerId(penalty.getReader().getId())
                        .readerName(penalty.getReader().getFullName())
                        .createdDate(penalty.getCreatedDate())
                        .paidDate(penalty.getPaidDate())
                        .build())
                .collect(Collectors.toList());
        
        byte[] data = exportService.exportPenaltyReport(response, format);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportService.getFileName("penalty-report", format) + "\"")
                .contentType(MediaType.parseMediaType(exportService.getContentType(format)))
                .body(data);
    }

    @GetMapping("/popular-books/export")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Export popular books", description = "Xuất top sách phổ biến ra file CSV hoặc Excel")
    public ResponseEntity<?> exportPopularBooks(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        List<BookStatisticsResponse> books = statisticsService.getTopBorrowedBooks(limit);
        byte[] data = exportService.exportPopularBooks(books, format);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportService.getFileName("popular-books", format) + "\"")
                .contentType(MediaType.parseMediaType(exportService.getContentType(format)))
                .body(data);
    }
}
