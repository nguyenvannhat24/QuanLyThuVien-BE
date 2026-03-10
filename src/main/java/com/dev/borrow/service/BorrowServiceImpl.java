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
    public BorrowResponse borrowBook(Long userId, Long bookId) {

        com.dev.user.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User is not active");
        }

        // Check unpaid penalties
        long unpaidCount = penaltyService.countUnpaidPenalties(userId);
        if (unpaidCount > 0) {
            throw new RuntimeException("Cannot borrow: user has " + unpaidCount + " unpaid penalties");
        }

        // Check borrow limit
        Integer maxBorrow = systemConfigService.getConfigValueAsInt("max_borrow_per_reader");
        if (maxBorrow == null) {
            maxBorrow = 5;
        }
        long currentBorrows = borrowRepository.countByUser_IdAndStatus(userId, BorrowStatus.BORROWING);
        if (currentBorrows >= maxBorrow) {
            throw new RuntimeException("Cannot borrow: reached limit of " + maxBorrow + " books");
        }

        BookCopy bookCopy = bookCopyRepository.findByBook_IdAndStatus(bookId, BookCopyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available copy for this book"));

        Book book = bookCopy.getBook();

        // Check if book reserved for someone else
        Optional<Reservation> notifiedReservation = reservationService.getTopNotifiedReservation(book);
        if (notifiedReservation.isPresent() && !notifiedReservation.get().getReader().getId().equals(userId)) {
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

        borrowRepository.save(borrow);

        // Fulfill reservation if exists for this user
        if (notifiedReservation.isPresent() && notifiedReservation.get().getReader().getId().equals(userId)) {
            reservationService.fulfillReservation(notifiedReservation.get().getReservationId());
        }

        return mapToResponse(borrow);
    }

    @Override
    @Transactional
    public BorrowResponse returnBorrow(Long id) {

        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getStatus() == BorrowStatus.RETURNED) {
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

            // Create penalty record
            penaltyService.createOverduePenalty(borrow);
        } else {
            borrow.setStatus(BorrowStatus.RETURNED);
            borrow.setFineAmount(java.math.BigDecimal.ZERO);
        }

        bookCopy.setStatus(BookCopyStatus.AVAILABLE);
        bookCopyRepository.save(bookCopy);
        borrowRepository.save(borrow);

        // Notify next person in reservation queue
        reservationService.notifyNextInQueue(bookCopy.getBook());

        return mapToResponse(borrow);
    }

    @Override
    @Transactional
    public BorrowResponse renewBorrow(Long id) {

        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getStatus() != BorrowStatus.BORROWING) {
            throw new RuntimeException("Can only renew borrowing records");
        }

        Integer maxRenewCount = systemConfigService.getConfigValueAsInt("max_renew_count");
        if (borrow.getRenewCount() >= maxRenewCount) {
            throw new RuntimeException("Maximum renew count reached");
        }

        if (LocalDate.now().isAfter(borrow.getDueDate())) {
            throw new RuntimeException("Cannot renew overdue borrow");
        }

        // Check if there are waiting reservations for this book
        int waitingCount = reservationService.countWaitingReservations(borrow.getBookCopy().getBook());
        if (waitingCount > 0) {
            throw new RuntimeException("Cannot renew: book has " + waitingCount + " waiting reservations");
        }

        Integer defaultBorrowDays = systemConfigService.getConfigValueAsInt("default_borrow_days");
        borrow.setDueDate(borrow.getDueDate().plusDays(defaultBorrowDays));
        borrow.setRenewCount(borrow.getRenewCount() + 1);

        borrowRepository.save(borrow);

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