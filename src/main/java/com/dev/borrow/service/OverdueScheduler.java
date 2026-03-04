package com.dev.borrow.service;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final BorrowRepository borrowRepository;

    @Scheduled(cron = "0 0 0 * * ?") 
    // Chạy mỗi ngày lúc 00:00
    public void checkOverdueBooks() {

        List<Borrow> borrows =
                borrowRepository.findByStatus(BorrowStatus.BORROWED);

        for (Borrow borrow : borrows) {
            if (LocalDate.now().isAfter(borrow.getDueDate())) {
                borrow.setStatus(BorrowStatus.LATE);
                borrowRepository.save(borrow);
            }
        }
    }
}