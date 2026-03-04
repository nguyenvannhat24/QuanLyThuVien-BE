package com.dev.borrow.service;

import com.dev.book.model.Book;
import com.dev.book.repository.BookRepository;
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
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    
    @Override
    public DashboardResponse getDashboard() {

        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long totalBorrowed =
                borrowRepository.countByStatus(BorrowStatus.BORROWED);
        long totalLate =
                borrowRepository.countByStatus(BorrowStatus.LATE);

        return DashboardResponse.builder()
                .totalBooks(totalBooks)
                .totalUsers(totalUsers)
                .totalBorrowedBooks(totalBorrowed)
                .totalLateBooks(totalLate)
                .build();
    }
    @Override
    public BorrowResponse borrowBook(String userId, String bookId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getQuantity() <= 0) {
            throw new RuntimeException("Book out of stock");
        }

        long currentBorrowed =
                borrowRepository.countByUserIdAndStatus(userId, BorrowStatus.BORROWED);

        if (currentBorrowed >= 3) {
            throw new RuntimeException("Maximum borrowed books reached");
        }

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        Borrow borrow = Borrow.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.BORROWED)
                .extendCount(0)
                .build();

        borrowRepository.save(borrow);

        return mapToResponse(borrow);
    }

    @Override
    public void returnBook(String borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Book already returned");
        }

        Book book = bookRepository.findById(borrow.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        LocalDate today = LocalDate.now();

        borrow.setReturnDate(today);

        if (today.isAfter(borrow.getDueDate())) {

            long daysLate = ChronoUnit.DAYS
                    .between(borrow.getDueDate(), today);

            long fine = daysLate * 5000;

            borrow.setFineAmount(fine);
            borrow.setStatus(BorrowStatus.LATE);

        } else {
            borrow.setStatus(BorrowStatus.RETURNED);
            borrow.setFineAmount(0L);
        }

        book.setQuantity(book.getQuantity() + 1);

        bookRepository.save(book);
        borrowRepository.save(borrow);
    }
    @Override
    public void extendBorrow(String borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.getExtendCount() >= 2) {
            throw new RuntimeException("Extend limit reached");
        }

        borrow.setDueDate(borrow.getDueDate().plusDays(7));
        borrow.setExtendCount(borrow.getExtendCount() + 1);

        borrowRepository.save(borrow);
    }

    @Override
    public List<BorrowResponse> getMyBorrows(String userId) {
        return borrowRepository.findByUserId(userId)
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
                .userId(borrow.getUserId())
                .bookId(borrow.getBookId())
                .borrowDate(borrow.getBorrowDate())
                .dueDate(borrow.getDueDate())
                .returnDate(borrow.getReturnDate())
                .status(borrow.getStatus())
                .build();
    }
}