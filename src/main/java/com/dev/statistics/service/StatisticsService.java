package com.dev.statistics.service;

import com.dev.statistics.dto.BookStatisticsResponse;
import com.dev.statistics.dto.BorrowTrendResponse;
import com.dev.statistics.dto.DashboardMetricsResponse;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {
    
    DashboardMetricsResponse getDashboardMetrics();
    
    List<BookStatisticsResponse> getTopBorrowedBooks(int limit);
    
    List<BorrowTrendResponse> getBorrowingTrends(LocalDate startDate, LocalDate endDate);
    
    Double getOverdueRate();
    
    Long getPenaltyRevenue();
}
