package com.dev.statistics.service;

import com.dev.book.repository.BookCopyRepository;
import com.dev.book.repository.BookRepository;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.penalty.model.PenaltyStatus;
import com.dev.penalty.repository.PenaltyRepository;
import com.dev.statistics.dto.BookStatisticsResponse;
import com.dev.statistics.dto.BorrowTrendResponse;
import com.dev.statistics.dto.DashboardMetricsResponse;
import com.dev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final PenaltyRepository penaltyRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics() {
        Long totalBooks = bookRepository.count();
        Long totalReaders = userRepository.count();
        Long totalBorrows = borrowRepository.count();
        Long overdueCount = borrowRepository.countByStatus(BorrowStatus.OVERDUE);

        YearMonth currentMonth = YearMonth.now();
        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate currentMonthEnd = currentMonth.atEndOfMonth();
        
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate previousMonthStart = previousMonth.atDay(1);
        LocalDate previousMonthEnd = previousMonth.atEndOfMonth();

        Long currentMonthBorrows = borrowRepository.countByBorrowDateBetween(currentMonthStart, currentMonthEnd);
        Long previousMonthBorrows = borrowRepository.countByBorrowDateBetween(previousMonthStart, previousMonthEnd);

        List<BookStatisticsResponse> topBorrowedBooks = getTopBorrowedBooks(10);

        return DashboardMetricsResponse.builder()
                .totalBooks(totalBooks)
                .totalReaders(totalReaders)
                .totalBorrows(totalBorrows)
                .overdueCount(overdueCount)
                .currentMonthBorrows(currentMonthBorrows)
                .previousMonthBorrows(previousMonthBorrows)
                .topBorrowedBooks(topBorrowedBooks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookStatisticsResponse> getTopBorrowedBooks(int limit) {
        return borrowRepository.findTopBorrowedBooks(PageRequest.of(0, limit))
                .stream()
                .map(result -> BookStatisticsResponse.builder()
                        .bookId((Long) result[0])
                        .title((String) result[1])
                        .isbn((String) result[2])
                        .borrowCount((Long) result[3])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowTrendResponse> getBorrowingTrends(LocalDate startDate, LocalDate endDate) {
        return borrowRepository.findBorrowingTrends(startDate, endDate)
                .stream()
                .map(result -> {
                    Integer year = (Integer) result[0];
                    Integer month = (Integer) result[1];
                    Long count = (Long) result[2];
                    String period = String.format("%d-%02d", year, month);
                    
                    return BorrowTrendResponse.builder()
                            .period(period)
                            .borrowCount(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getOverdueRate() {
        Long totalBorrows = borrowRepository.count();
        if (totalBorrows == 0) {
            return 0.0;
        }
        Long overdueCount = borrowRepository.countByStatus(BorrowStatus.OVERDUE);
        return (overdueCount.doubleValue() / totalBorrows.doubleValue()) * 100.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPenaltyRevenue() {
        BigDecimal revenue = penaltyRepository.sumAmountByStatus(PenaltyStatus.PAID);
        return revenue != null ? revenue.longValue() : 0L;
    }
}
