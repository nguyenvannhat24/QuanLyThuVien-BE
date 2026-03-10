package com.dev.borrow.service;

import com.dev.book.model.Book;
import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import com.dev.book.repository.BookCopyRepository;
import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;
import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.config.service.SystemConfigService;
import com.dev.notification.service.NotificationService;
import com.dev.penalty.service.PenaltyService;
import com.dev.reservation.model.Reservation;
import com.dev.reservation.model.ReservationStatus;
import com.dev.reservation.service.ReservationService;
import com.dev.user.model.UserStatus;
import com.dev.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {
    
    private static final Logger log = LoggerFactory.getLogger(BorrowServiceImpl.class);

    private final BorrowRepository borrowRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;
    private final PenaltyService penaltyService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    
    @Override
    public DashboardResponse getDashboard() {

        long totalBooks = bookCopyRepository.count();
        long totalUsers = userRepository.count();
        long totalBorrowed =
                borrowRepository.countByStatus(BorrowStatus.BORROWING);
        long totalLate =
                borrowRepository.countByStatus(BorrowStatus.OVERDUE);

        return DashboardResponse.builder()
                .totalBooks(totalBooks)
                .totalUsers(totalUsers)
                .totalBorrowedBooks(totalBorrowed)
                .totalLateBooks(totalLate)
                .build();
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "topBooks", allEntries = true)
    public BorrowResponse borrowBook(Long userId, Long bookId) {
        log.info("Borrow request - userId: {}, bookId: {}", userId, bookId);

        com.dev.user.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Borrow denied - user not active: userId={}", userId);
            throw new RuntimeException("User is not active");
        }

        long unpaidCount = penaltyService.countUnpaidPenalties(userId);
        if (unpaidCount > 0) {
            log.warn("Borrow denied - unpaid penalties: userId={}, count={}", userId, unpaidCount);
            throw new RuntimeException("Cannot borrow: user has " + unpaidCount + " unpaid penalties");
        }

        Integer maxBorrow = systemConfigService.getConfigValueAsInt("max_borrow_per_reader");
        if (maxBorrow == null) {
            maxBorrow = 5;
        }
        long currentBorrows = borrowRepository.countByUser_IdAndStatus(userId, BorrowStatus.BORROWING);
        if (currentBorrows >= maxBorrow) {
            log.warn("Borrow denied - limit reached: userId={}, current={}, limit={}", userId, currentBorrows, maxBorrow);
            throw new RuntimeException("Cannot borrow: reached limit of " + maxBorrow + " books");
        }

        BookCopy bookCopy = bookCopyRepository.findByBook_IdAndStatus(bookId, BookCopyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available copy for this book"));

        Book book = bookCopy.getBook();

        Optional<Reservation> notifiedReservation = reservationService.getTopNotifiedReservation(book);
        if (notifiedReservation.isPresent() && !notifiedReservation.get().getReader().getId().equals(userId)) {
            log.warn("Borrow denied - book reserved for another user: bookId={}", bookId);
            throw new RuntimeException("This book is reserved for another user");
        }

        bookCopy.setStatus(BookCopyStatus.BORROWED);
        bookCopyRepository.save(bookCopy);

        Integer defaultBorrowDays = systemConfigService.getConfigValueAsInt("default_borrow_days");

        Borrow borrow = Borrow.builder()
                .user(user)
                .bookCopy(bookCopy)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(defaultBorrowDays))
                .status(BorrowStatus.BORROWING)
                .renewCount(0)
                .build();

        borrow = borrowRepository.save(borrow);

        if (notifiedReservation.isPresent() && notifiedReservation.get().getReader().getId().equals(userId)) {
            reservationService.fulfillReservation(notifiedReservation.get().getReservationId());
        }

        log.info("Book borrowed successfully - borrowId: {}, userId: {}, bookId: {}", borrow.getId(), userId, bookId);

        return mapToResponse(borrow);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "topBooks", allEntries = true)
    public BorrowResponse returnBorrow(Long id) {
        log.info("Return request - borrowId: {}", id);

        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            log.warn("Return denied - already returned: borrowId={}", id);
            throw new RuntimeException("Book already returned");
        }

        BookCopy bookCopy = borrow.getBookCopy();
        LocalDate today = LocalDate.now();

        borrow.setReturnDate(today);

        if (today.isAfter(borrow.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(borrow.getDueDate(), today);
            Integer finePerDay = systemConfigService.getConfigValueAsInt("fine_per_day");
            long fine = daysLate * finePerDay;
            
            borrow.setFineAmount(new java.math.BigDecimal(fine));
            borrow.setStatus(BorrowStatus.OVERDUE);

            penaltyService.createOverduePenalty(borrow);
            log.warn("Book returned late - borrowId: {}, daysLate: {}, fine: {}", id, daysLate, fine);
        } else {
            borrow.setStatus(BorrowStatus.RETURNED);
            borrow.setFineAmount(java.math.BigDecimal.ZERO);
            log.info("Book returned on time - borrowId: {}", id);
        }

        bookCopy.setStatus(BookCopyStatus.AVAILABLE);
        bookCopyRepository.save(bookCopy);
        borrowRepository.save(borrow);

        reservationService.notifyNextInQueue(bookCopy.getBook());

        return mapToResponse(borrow);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "topBooks", allEntries = true)
    public BorrowResponse renewBorrow(Long id) {
        log.info("Renew request - borrowId: {}", id);

        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getStatus() != BorrowStatus.BORROWING) {
            log.warn("Renew denied - invalid status: borrowId={}, status={}", id, borrow.getStatus());
            throw new RuntimeException("Can only renew borrowing records");
        }

        Integer maxRenewCount = systemConfigService.getConfigValueAsInt("max_renew_count");
        if (borrow.getRenewCount() >= maxRenewCount) {
            log.warn("Renew denied - max renewals reached: borrowId={}, count={}", id, borrow.getRenewCount());
            throw new RuntimeException("Maximum renew count reached");
        }

        if (LocalDate.now().isAfter(borrow.getDueDate())) {
            log.warn("Renew denied - overdue: borrowId={}", id);
            throw new RuntimeException("Cannot renew overdue borrow");
        }

        int waitingCount = reservationService.countWaitingReservations(borrow.getBookCopy().getBook());
        if (waitingCount > 0) {
            log.warn("Renew denied - waiting reservations: borrowId={}, waitingCount={}", id, waitingCount);
            throw new RuntimeException("Cannot renew: book has " + waitingCount + " waiting reservations");
        }

        Integer defaultBorrowDays = systemConfigService.getConfigValueAsInt("default_borrow_days");
        borrow.setDueDate(borrow.getDueDate().plusDays(defaultBorrowDays));
        borrow.setRenewCount(borrow.getRenewCount() + 1);

        borrowRepository.save(borrow);
        
        log.info("Borrow renewed successfully - borrowId: {}, newRenewCount: {}", id, borrow.getRenewCount());

        return mapToResponse(borrow);
    }

    @Override
    public List<BorrowResponse> getMyBorrows(Long userId) {
        return borrowRepository.findByUser_IdWithDetails(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getAll() {
        return borrowRepository.findAllWithDetails()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BorrowResponse mapToResponse(Borrow borrow) {
        return BorrowResponse.builder()
                .id(borrow.getId())
                .userId(borrow.getUser().getId())
                .bookId(borrow.getBookCopy().getBook().getId())
                .borrowDate(borrow.getBorrowDate())
                .dueDate(borrow.getDueDate())
                .returnDate(borrow.getReturnDate())
                .status(borrow.getStatus())
                .build();
    }
}