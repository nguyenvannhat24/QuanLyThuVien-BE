package com.dev.scheduler;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.book.model.Book;
import com.dev.email.service.EmailService;
import com.dev.notification.model.Notification;
import com.dev.notification.model.NotificationType;
import com.dev.notification.repository.NotificationRepository;
import com.dev.penalty.model.Penalty;
import com.dev.penalty.model.PenaltyStatus;
import com.dev.penalty.repository.PenaltyRepository;
import com.dev.reservation.model.Reservation;
import com.dev.reservation.model.ReservationStatus;
import com.dev.reservation.repository.ReservationRepository;
import com.dev.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final BorrowRepository borrowRepository;
    private final ReservationRepository reservationRepository;
    private final PenaltyRepository penaltyRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkOverdueBorrowsAndNotify() {
        log.info("Starting scheduled task: checkOverdueBorrowsAndNotify");

        LocalDate today = LocalDate.now();
        List<Borrow> overdueBorrows = borrowRepository.findByStatusAndDueDateBeforeWithDetails(
                BorrowStatus.BORROWING, today);

        for (Borrow borrow : overdueBorrows) {
            try {
                User user = borrow.getUser();
                int daysOverdue = (int) ChronoUnit.DAYS.between(borrow.getDueDate(), today);

                notificationRepository.save(Notification.builder()
                        .user(user)
                        .title("Sách quá hạn")
                        .message("Sách \"" + borrow.getBookCopy().getBook().getTitle() + 
                                "\" đã quá hạn " + daysOverdue + " ngày. Vui lòng trả sách sớm nhất có thể.")
                        .type(NotificationType.OVERDUE_REMINDER)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build());

                emailService.sendOverdueReminderEmail(user, borrow, daysOverdue);

                log.info("Sent overdue reminder to user {} for borrow {}", user.getId(), borrow.getId());
            } catch (Exception e) {
                log.error("Failed to send overdue notification for borrow {}: {}", 
                        borrow.getId(), e.getMessage());
            }
        }

        log.info("Completed checkOverdueBorrowsAndNotify. Processed {} overdue borrows", overdueBorrows.size());
    }

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkUpcomingDueDateAndNotify() {
        log.info("Starting scheduled task: checkUpcomingDueDateAndNotify");

        LocalDate today = LocalDate.now();
        LocalDate twoDaysLater = today.plusDays(2);

        List<Borrow> upcomingDueBorrows = borrowRepository.findByStatusAndDueDateBeforeWithDetails(
                BorrowStatus.BORROWING, twoDaysLater);

        for (Borrow borrow : upcomingDueBorrows) {
            if (borrow.getDueDate().isAfter(today)) {
                try {
                    User user = borrow.getUser();
                    long daysUntilDue = ChronoUnit.DAYS.between(today, borrow.getDueDate());

                    notificationRepository.save(Notification.builder()
                            .user(user)
                            .title("Nhắc nhở sắp đến hạn trả sách")
                            .message("Sách \"" + borrow.getBookCopy().getBook().getTitle() + 
                                    "\" sẽ đến hạn trả trong " + daysUntilDue + " ngày.")
                            .type(NotificationType.BORROW_REMINDER)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build());

                    log.info("Sent upcoming due reminder to user {} for borrow {}", user.getId(), borrow.getId());
                } catch (Exception e) {
                    log.error("Failed to send upcoming due notification for borrow {}: {}", 
                            borrow.getId(), e.getMessage());
                }
            }
        }

        log.info("Completed checkUpcomingDueDateAndNotify. Processed {} upcoming due borrows", upcomingDueBorrows.size());
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void checkExpiredReservationsAndNotify() {
        log.info("Starting scheduled task: checkExpiredReservationsAndNotify");

        LocalDate today = LocalDate.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpireDateBeforeWithBook(ReservationStatus.NOTIFIED, today);

        for (Reservation reservation : expiredReservations) {
            try {
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                Book book = reservation.getBook();
                List<Reservation> waitingReservations = reservationRepository
                        .findByBookAndStatusOrderByQueuePositionAsc(book, ReservationStatus.WAITING);

                if (!waitingReservations.isEmpty()) {
                    Reservation nextReservation = waitingReservations.get(0);
                    nextReservation.setStatus(ReservationStatus.NOTIFIED);
                    nextReservation.setNotifyDate(today);
                    nextReservation.setExpireDate(today.plusDays(3));
                    reservationRepository.save(nextReservation);

                    User nextUser = nextReservation.getReader();
                    notificationRepository.save(Notification.builder()
                            .user(nextUser)
                            .title("Sách đã có sẵn - Đặt chỗ")
                            .message("Sách \"" + book.getTitle() + "\" đã có sẵn. Vui lòng đến Thư viện trong 3 ngày.")
                            .type(NotificationType.RESERVATION_READY)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build());

                    emailService.sendReservationReadyEmail(nextUser, book);

                    log.info("Notified next user {} for book {}", nextUser.getId(), book.getId());
                }

                log.info("Expired reservation {} for user {}", reservation.getReservationId(), reservation.getReader().getId());
            } catch (Exception e) {
                log.error("Failed to process expired reservation {}: {}", reservation.getReservationId(), e.getMessage());
            }
        }

        log.info("Completed checkExpiredReservationsAndNotify. Processed {} expired reservations", expiredReservations.size());
    }

    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void checkUnpaidPenaltiesAndNotify() {
        log.info("Starting scheduled task: checkUnpaidPenaltiesAndNotify");

        List<Penalty> unpaidPenalties = penaltyRepository.findByStatusWithDetails(PenaltyStatus.UNPAID);

        for (Penalty penalty : unpaidPenalties) {
            try {
                User user = penalty.getReader();

                notificationRepository.save(Notification.builder()
                        .user(user)
                        .title("Nhắc nhở thanh toán phạt")
                        .message("Bạn có khoản phạt chưa thanh toán: " + penalty.getType() + 
                                " - " + penalty.getAmount() + " VNĐ")
                        .type(NotificationType.PENALTY)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build());

                emailService.sendPenaltyNotificationEmail(user, penalty);

                log.info("Sent unpaid penalty reminder to user {} for penalty {}", user.getId(), penalty.getPenaltyId());
            } catch (Exception e) {
                log.error("Failed to send penalty notification for penalty {}: {}", 
                        penalty.getPenaltyId(), e.getMessage());
            }
        }

        log.info("Completed checkUnpaidPenaltiesAndNotify. Processed {} unpaid penalties", unpaidPenalties.size());
    }
}