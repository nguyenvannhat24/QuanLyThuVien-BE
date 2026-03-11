package com.dev.borrow.repository;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.book.model.Book;
import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import com.dev.book.repository.BookCopyRepository;
import com.dev.book.repository.BookRepository;
import com.dev.user.model.User;
import com.dev.user.model.UserStatus;
import com.dev.auth.model.Role;
import com.dev.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BorrowRepositoryTest {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    private User testUser;
    private Book testBook;
    private BookCopy testBookCopy;

    @BeforeEach
    void setUp() {
        borrowRepository.deleteAll();
        bookCopyRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.READER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        testBook = Book.builder()
                .title("Test Book")
                .isbn("978-0-123456-78-9")
                .publishYear(2023)
                .build();
        testBook = bookRepository.save(testBook);

        testBookCopy = BookCopy.builder()
                .book(testBook)
                .copyCode("COPY-001")
                .status(BookCopyStatus.AVAILABLE)
                .build();
        testBookCopy = bookCopyRepository.save(testBookCopy);
    }

    @Test
    void testCountByUser_IdAndStatus() {
        // Given
        Borrow borrow1 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        Borrow borrow2 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        borrowRepository.saveAll(List.of(borrow1, borrow2));

        // When
        long count = borrowRepository.countByUser_IdAndStatus(testUser.getId(), BorrowStatus.BORROWING);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testExistsByUser_IdAndStatus() {
        // Given
        Borrow borrow = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        borrowRepository.save(borrow);

        // When
        boolean exists = borrowRepository.existsByUser_IdAndStatus(testUser.getId(), BorrowStatus.BORROWING);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testFindByUser_IdWithDetails_PreventN1() {
        // Given
        Borrow borrow = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        borrowRepository.save(borrow);

        // When - Using JOIN FETCH should load all relations in one query
        List<Borrow> borrows = borrowRepository.findByUser_IdWithDetails(testUser.getId());

        // Then
        assertThat(borrows).hasSize(1);
        Borrow foundBorrow = borrows.get(0);
        
        // Verify fetch joins work - these should not trigger additional queries
        assertThat(foundBorrow.getUser().getUsername()).isEqualTo("testuser");
        assertThat(foundBorrow.getBookCopy().getCopyCode()).isEqualTo("COPY-001");
        assertThat(foundBorrow.getBookCopy().getBook().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindAllWithDetails_PreventN1() {
        // Given
        Borrow borrow1 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        Borrow borrow2 = createBorrow(testUser, testBookCopy, BorrowStatus.RETURNED);
        borrowRepository.saveAll(List.of(borrow1, borrow2));

        // When
        List<Borrow> borrows = borrowRepository.findAllWithDetails();

        // Then
        assertThat(borrows).hasSize(2);
        borrows.forEach(borrow -> {
            assertThat(borrow.getUser()).isNotNull();
            assertThat(borrow.getBookCopy()).isNotNull();
            assertThat(borrow.getBookCopy().getBook()).isNotNull();
        });
    }

    @Test
    void testFindByStatusAndDueDateBeforeWithDetails() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        Borrow overdueBorrow = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        overdueBorrow.setDueDate(yesterday);
        borrowRepository.save(overdueBorrow);

        // When
        List<Borrow> overdues = borrowRepository.findByStatusAndDueDateBeforeWithDetails(
                BorrowStatus.BORROWING, today);

        // Then
        assertThat(overdues).hasSize(1);
        assertThat(overdues.get(0).getDueDate()).isBefore(today);
        assertThat(overdues.get(0).getStatus()).isEqualTo(BorrowStatus.BORROWING);
    }

    @Test
    void testCountByBorrowDateBetween() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        Borrow borrow1 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        borrow1.setBorrowDate(startDate.plusDays(1));
        
        Borrow borrow2 = createBorrow(testUser, testBookCopy, BorrowStatus.RETURNED);
        borrow2.setBorrowDate(startDate.plusDays(3));
        
        borrowRepository.saveAll(List.of(borrow1, borrow2));

        // When
        long count = borrowRepository.countByBorrowDateBetween(startDate, endDate);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindTopBorrowedBooks() {
        // Given
        BookCopy copy2 = BookCopy.builder()
                .book(testBook)
                .copyCode("COPY-002")
                .status(BookCopyStatus.AVAILABLE)
                .build();
        bookCopyRepository.save(copy2);

        // Create multiple borrows for the same book
        Borrow borrow1 = createBorrow(testUser, testBookCopy, BorrowStatus.RETURNED);
        Borrow borrow2 = createBorrow(testUser, copy2, BorrowStatus.RETURNED);
        Borrow borrow3 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        borrowRepository.saveAll(List.of(borrow1, borrow2, borrow3));

        // When
        List<Object[]> topBooks = borrowRepository.findTopBorrowedBooks(PageRequest.of(0, 5));

        // Then
        assertThat(topBooks).isNotEmpty();
        Object[] topBook = topBooks.get(0);
        assertThat(topBook).hasSize(4);
        assertThat(topBook[0]).isEqualTo(testBook.getId());
        assertThat(topBook[1]).isEqualTo("Test Book");
        assertThat((Long) topBook[3]).isEqualTo(3L); // 3 borrows
    }

    @Test
    void testFindBorrowingTrends() {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(2);
        LocalDate endDate = LocalDate.now();
        
        Borrow borrow1 = createBorrow(testUser, testBookCopy, BorrowStatus.RETURNED);
        borrow1.setBorrowDate(startDate.plusDays(5));
        
        Borrow borrow2 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        borrow2.setBorrowDate(startDate.plusDays(10));
        
        borrowRepository.saveAll(List.of(borrow1, borrow2));

        // When
        List<Object[]> trends = borrowRepository.findBorrowingTrends(startDate, endDate);

        // Then
        assertThat(trends).isNotEmpty();
    }

    @Test
    void testCountByStatus() {
        // Given
        Borrow borrow1 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        Borrow borrow2 = createBorrow(testUser, testBookCopy, BorrowStatus.BORROWING);
        Borrow borrow3 = createBorrow(testUser, testBookCopy, BorrowStatus.RETURNED);
        borrowRepository.saveAll(List.of(borrow1, borrow2, borrow3));

        // When
        long borrowingCount = borrowRepository.countByStatus(BorrowStatus.BORROWING);
        long returnedCount = borrowRepository.countByStatus(BorrowStatus.RETURNED);

        // Then
        assertThat(borrowingCount).isEqualTo(2);
        assertThat(returnedCount).isEqualTo(1);
    }

    private Borrow createBorrow(User user, BookCopy bookCopy, BorrowStatus status) {
        return Borrow.builder()
                .user(user)
                .bookCopy(bookCopy)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(status)
                .renewCount(0)
                .fineAmount(BigDecimal.ZERO)
                .build();
    }
}
