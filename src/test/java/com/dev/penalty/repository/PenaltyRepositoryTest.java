package com.dev.penalty.repository;

import com.dev.penalty.model.Penalty;
import com.dev.penalty.model.PenaltyStatus;
import com.dev.penalty.model.PenaltyType;
import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.book.model.Book;
import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import com.dev.user.model.User;
import com.dev.user.model.UserStatus;
import com.dev.auth.model.Role;
import com.dev.user.repository.UserRepository;
import com.dev.book.repository.BookRepository;
import com.dev.book.repository.BookCopyRepository;
import com.dev.borrow.repository.BorrowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PenaltyRepositoryTest {

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    private User testUser;
    private Borrow testBorrow;

    @BeforeEach
    void setUp() {
        penaltyRepository.deleteAll();
        borrowRepository.deleteAll();
        bookCopyRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        Book testBook = Book.builder()
                .title("Test Book")
                .isbn("978-0-123456-78-9")
                .publishYear(2023)
                .build();
        testBook = bookRepository.save(testBook);

        BookCopy testBookCopy = BookCopy.builder()
                .book(testBook)
                .copyCode("COPY-001")
                .status(BookCopyStatus.AVAILABLE)
                .build();
        testBookCopy = bookCopyRepository.save(testBookCopy);

        testBorrow = Borrow.builder()
                .user(testUser)
                .bookCopy(testBookCopy)
                .borrowDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(16))
                .status(BorrowStatus.OVERDUE)
                .renewCount(0)
                .fineAmount(BigDecimal.ZERO)
                .build();
        testBorrow = borrowRepository.save(testBorrow);
    }

    @Test
    void testCountByReaderAndStatus() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.00"));
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("20.00"));
        Penalty penalty3 = createPenalty(testUser, testBorrow, PenaltyStatus.PAID, new BigDecimal("15.00"));
        penaltyRepository.saveAll(List.of(penalty1, penalty2, penalty3));

        // When
        long unpaidCount = penaltyRepository.countByReaderAndStatus(testUser, PenaltyStatus.UNPAID);
        long paidCount = penaltyRepository.countByReaderAndStatus(testUser, PenaltyStatus.PAID);

        // Then
        assertThat(unpaidCount).isEqualTo(2);
        assertThat(paidCount).isEqualTo(1);
    }

    @Test
    void testFindByReaderOrderByCreatedDateDescWithDetails_PreventN1() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.00"));
        penalty1.setCreatedDate(LocalDate.now().minusDays(2));
        
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.PAID, new BigDecimal("20.00"));
        penalty2.setCreatedDate(LocalDate.now().minusDays(1));
        
        penaltyRepository.saveAll(List.of(penalty1, penalty2));

        // When
        List<Penalty> penalties = penaltyRepository.findByReaderOrderByCreatedDateDescWithDetails(testUser);

        // Then
        assertThat(penalties).hasSize(2);
        assertThat(penalties.get(0).getCreatedDate()).isAfter(penalties.get(1).getCreatedDate());
        
        penalties.forEach(penalty -> {
            assertThat(penalty.getReader()).isNotNull();
            assertThat(penalty.getBorrowRecord()).isNotNull();
        });
    }

    @Test
    void testFindByStatus() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.00"));
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("20.00"));
        Penalty penalty3 = createPenalty(testUser, testBorrow, PenaltyStatus.PAID, new BigDecimal("15.00"));
        penaltyRepository.saveAll(List.of(penalty1, penalty2, penalty3));

        // When
        List<Penalty> unpaid = penaltyRepository.findByStatus(PenaltyStatus.UNPAID);
        List<Penalty> paid = penaltyRepository.findByStatus(PenaltyStatus.PAID);

        // Then
        assertThat(unpaid).hasSize(2);
        assertThat(paid).hasSize(1);
    }

    @Test
    void testFindByBorrowRecord() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.00"));
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.PAID, new BigDecimal("20.00"));
        penaltyRepository.saveAll(List.of(penalty1, penalty2));

        // When
        List<Penalty> penalties = penaltyRepository.findByBorrowRecord(testBorrow);

        // Then
        assertThat(penalties).hasSize(2);
    }

    @Test
    void testSumAmountByStatus() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.50"));
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("20.75"));
        Penalty penalty3 = createPenalty(testUser, testBorrow, PenaltyStatus.PAID, new BigDecimal("15.00"));
        penaltyRepository.saveAll(List.of(penalty1, penalty2, penalty3));

        // When
        BigDecimal unpaidSum = penaltyRepository.sumAmountByStatus(PenaltyStatus.UNPAID);
        BigDecimal paidSum = penaltyRepository.sumAmountByStatus(PenaltyStatus.PAID);

        // Then
        assertThat(unpaidSum).isEqualByComparingTo(new BigDecimal("31.25"));
        assertThat(paidSum).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void testFindByStatusWithDetails_PreventN1() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.00"));
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("20.00"));
        penaltyRepository.saveAll(List.of(penalty1, penalty2));

        // When
        List<Penalty> penalties = penaltyRepository.findByStatusWithDetails(PenaltyStatus.UNPAID);

        // Then
        assertThat(penalties).hasSize(2);
        penalties.forEach(penalty -> {
            assertThat(penalty.getReader()).isNotNull();
            assertThat(penalty.getBorrowRecord()).isNotNull();
            assertThat(penalty.getBorrowRecord().getBookCopy()).isNotNull();
            assertThat(penalty.getBorrowRecord().getBookCopy().getBook()).isNotNull();
        });
    }

    @Test
    void testFindAllWithDetails_PreventN1() {
        // Given
        Penalty penalty1 = createPenalty(testUser, testBorrow, PenaltyStatus.UNPAID, new BigDecimal("10.00"));
        Penalty penalty2 = createPenalty(testUser, testBorrow, PenaltyStatus.PAID, new BigDecimal("20.00"));
        penaltyRepository.saveAll(List.of(penalty1, penalty2));

        // When
        List<Penalty> penalties = penaltyRepository.findAllWithDetails();

        // Then
        assertThat(penalties).hasSize(2);
        penalties.forEach(penalty -> {
            assertThat(penalty.getReader()).isNotNull();
            assertThat(penalty.getBorrowRecord()).isNotNull();
        });
    }

    private Penalty createPenalty(User reader, Borrow borrowRecord, PenaltyStatus status, BigDecimal amount) {
        return Penalty.builder()
                .reader(reader)
                .borrowRecord(borrowRecord)
                .type(PenaltyType.OVERDUE)
                .amount(amount)
                .status(status)
                .createdDate(LocalDate.now())
                .build();
    }
}
