package com.dev.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMetricsResponse {
    private Long totalBooks;
    private Long totalReaders;
    private Long totalBorrows;
    private Long overdueCount;
    private Long currentMonthBorrows;
    private Long previousMonthBorrows;
    private List<BookStatisticsResponse> topBorrowedBooks;
}
