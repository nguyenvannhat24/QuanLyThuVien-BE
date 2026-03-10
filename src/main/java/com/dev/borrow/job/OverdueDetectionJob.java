package com.dev.borrow.job;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueDetectionJob {

    private final BorrowRepository borrowRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void detectOverdueBorrows() {
        log.info("Starting overdue detection job...");
        
        List<Borrow> overdueBorrows = borrowRepository
                .findByStatusAndDueDateBefore(BorrowStatus.BORROWING, LocalDate.now());
        
        if (!overdueBorrows.isEmpty()) {
            overdueBorrows.forEach(borrow -> borrow.setStatus(BorrowStatus.OVERDUE));
            borrowRepository.saveAll(overdueBorrows);
            log.info("Marked {} borrow(s) as OVERDUE", overdueBorrows.size());
        } else {
            log.info("No overdue borrows found");
        }
        
        log.info("Overdue detection job completed");
    }
}
