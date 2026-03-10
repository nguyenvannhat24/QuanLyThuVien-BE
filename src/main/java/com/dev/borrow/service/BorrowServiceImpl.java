package com.dev.borrow.service;

import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import com.dev.book.repository.BookCopyRepository;
import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;
import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    
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
    public BorrowResponse borrowBook(Long userId, Long bookId) {

        com.dev.user.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BookCopy bookCopy = bookCopyRepository.findByBook_IdAndStatus(bookId, BookCopyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available copy for this book"));

        long currentBorrowed =
                borrowRepository.countByUser_IdAndStatus(userId, BorrowStatus.BORROWING);

        if (currentBorrowed >= 3) {
            throw new RuntimeException("Maximum borrowed books reached");
        }

        bookCopy.setStatus(BookCopyStatus.BORROWED);
        bookCopyRepository.save(bookCopy);

        Borrow borrow = Borrow.builder()
                .user(user)
                .bookCopy(bookCopy)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.BORROWING)
                .renewCount(0)
                .build();

        borrowRepository.save(borrow);

        return mapToResponse(borrow);
    }

    @Override
    public void returnBook(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Book already returned");
        }

        BookCopy bookCopy = borrow.getBookCopy();

        LocalDate today = LocalDate.now();

        borrow.setReturnDate(today);

        if (today.isAfter(borrow.getDueDate())) {

            long daysLate = ChronoUnit.DAYS
                    .between(borrow.getDueDate(), today);

            long fine = daysLate * 5000;

            borrow.setFineAmount(new java.math.BigDecimal(fine));
            borrow.setStatus(BorrowStatus.OVERDUE);

        } else {
            borrow.setStatus(BorrowStatus.RETURNED);
            borrow.setFineAmount(java.math.BigDecimal.ZERO);
        }

        bookCopy.setStatus(BookCopyStatus.AVAILABLE);

        bookCopyRepository.save(bookCopy);
        borrowRepository.save(borrow);
    }
    @Override
    public void extendBorrow(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getRenewCount() >= 2) {
            throw new RuntimeException("Extend limit reached");
        }

        borrow.setDueDate(borrow.getDueDate().plusDays(7));
        borrow.setRenewCount(borrow.getRenewCount() + 1);

        borrowRepository.save(borrow);
    }

    @Override
    public List<BorrowResponse> getMyBorrows(Long userId) {
        return borrowRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getAll() {
        return borrowRepository.findAll()
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