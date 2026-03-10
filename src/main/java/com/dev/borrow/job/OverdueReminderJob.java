package com.dev.borrow.job;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.notification.model.NotificationType;
import com.dev.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueReminderJob {
    private final BorrowRepository borrowRepository;
    private final NotificationService notificationService;
    
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendOverdueReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Borrow> dueSoon = borrowRepository
                .findByStatusAndDueDateBeforeWithDetails(BorrowStatus.BORROWING, tomorrow);
        
        for (Borrow b : dueSoon) {
            notificationService.createNotification(
                b.getUser(),
                "Book Due Tomorrow",
                "Your book '" + b.getBookCopy().getBook().getTitle() + "' is due tomorrow. Please return on time to avoid fines.",
                NotificationType.OVERDUE_REMINDER
            );
        }
    }
}
