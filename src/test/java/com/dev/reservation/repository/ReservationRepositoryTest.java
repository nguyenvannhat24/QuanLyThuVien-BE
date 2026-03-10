package com.dev.reservation.repository;

import com.dev.reservation.model.Reservation;
import com.dev.reservation.model.ReservationStatus;
import com.dev.book.model.Book;
import com.dev.user.model.User;
import com.dev.user.model.UserStatus;
import com.dev.auth.model.Role;
import com.dev.user.repository.UserRepository;
import com.dev.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
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

        testBook = Book.builder()
                .title("Test Book")
                .isbn("978-0-123456-78-9")
                .publishYear(2023)
                .build();
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testFindByBookAndStatusOrderByQueuePositionAsc() {
        // Given
        Reservation res1 = createReservation(testUser, testBook, ReservationStatus.WAITING, 2);
        Reservation res2 = createReservation(testUser, testBook, ReservationStatus.WAITING, 1);
        Reservation res3 = createReservation(testUser, testBook, ReservationStatus.WAITING, 3);
        reservationRepository.saveAll(List.of(res1, res2, res3));

        // When
        List<Reservation> reservations = reservationRepository.findByBookAndStatusOrderByQueuePositionAsc(
                testBook, ReservationStatus.WAITING);

        // Then
        assertThat(reservations).hasSize(3);
        assertThat(reservations.get(0).getQueuePosition()).isEqualTo(1);
        assertThat(reservations.get(1).getQueuePosition()).isEqualTo(2);
        assertThat(reservations.get(2).getQueuePosition()).isEqualTo(3);
    }

    @Test
    void testFindByStatusAndExpireDateBefore() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        Reservation expiredRes = createReservation(testUser, testBook, ReservationStatus.NOTIFIED, 1);
        expiredRes.setExpireDate(yesterday);
        reservationRepository.save(expiredRes);

        // When
        List<Reservation> expired = reservationRepository.findByStatusAndExpireDateBefore(
                ReservationStatus.NOTIFIED, today);

        // Then
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getExpireDate()).isBefore(today);
    }

    @Test
    void testFindByStatusAndExpireDateBeforeWithBook_PreventN1() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        Reservation expiredRes = createReservation(testUser, testBook, ReservationStatus.NOTIFIED, 1);
        expiredRes.setExpireDate(yesterday);
        reservationRepository.save(expiredRes);

        // When
        List<Reservation> expired = reservationRepository.findByStatusAndExpireDateBeforeWithBook(
                ReservationStatus.NOTIFIED, today);

        // Then
        assertThat(expired).hasSize(1);
        Reservation found = expired.get(0);
        assertThat(found.getBook().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByReaderOrderByReserveDateDescWithDetails_PreventN1() {
        // Given
        Reservation res1 = createReservation(testUser, testBook, ReservationStatus.WAITING, 1);
        res1.setReserveDate(LocalDate.now().minusDays(2));
        
        Reservation res2 = createReservation(testUser, testBook, ReservationStatus.FULFILLED, 1);
        res2.setReserveDate(LocalDate.now().minusDays(1));
        
        reservationRepository.saveAll(List.of(res1, res2));

        // When
        List<Reservation> reservations = reservationRepository.findByReaderOrderByReserveDateDescWithDetails(testUser);

        // Then
        assertThat(reservations).hasSize(2);
        assertThat(reservations.get(0).getReserveDate()).isAfter(reservations.get(1).getReserveDate());
        
        reservations.forEach(res -> {
            assertThat(res.getBook()).isNotNull();
            assertThat(res.getReader()).isNotNull();
            assertThat(res.getBook().getTitle()).isEqualTo("Test Book");
        });
    }

    @Test
    void testExistsByReaderAndBookAndStatusIn() {
        // Given
        Reservation reservation = createReservation(testUser, testBook, ReservationStatus.WAITING, 1);
        reservationRepository.save(reservation);

        // When
        boolean exists = reservationRepository.existsByReaderAndBookAndStatusIn(
                testUser, testBook, List.of(ReservationStatus.WAITING, ReservationStatus.NOTIFIED));

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testFindTopByBookAndStatusOrderByQueuePositionAsc() {
        // Given
        Reservation res1 = createReservation(testUser, testBook, ReservationStatus.WAITING, 3);
        Reservation res2 = createReservation(testUser, testBook, ReservationStatus.WAITING, 1);
        Reservation res3 = createReservation(testUser, testBook, ReservationStatus.WAITING, 2);
        reservationRepository.saveAll(List.of(res1, res2, res3));

        // When
        Optional<Reservation> first = reservationRepository.findTopByBookAndStatusOrderByQueuePositionAsc(
                testBook, ReservationStatus.WAITING);

        // Then
        assertThat(first).isPresent();
        assertThat(first.get().getQueuePosition()).isEqualTo(1);
    }

    @Test
    void testCountByBookAndStatus() {
        // Given
        Reservation res1 = createReservation(testUser, testBook, ReservationStatus.WAITING, 1);
        Reservation res2 = createReservation(testUser, testBook, ReservationStatus.WAITING, 2);
        Reservation res3 = createReservation(testUser, testBook, ReservationStatus.FULFILLED, 1);
        reservationRepository.saveAll(List.of(res1, res2, res3));

        // When
        int waitingCount = reservationRepository.countByBookAndStatus(testBook, ReservationStatus.WAITING);
        int fulfilledCount = reservationRepository.countByBookAndStatus(testBook, ReservationStatus.FULFILLED);

        // Then
        assertThat(waitingCount).isEqualTo(2);
        assertThat(fulfilledCount).isEqualTo(1);
    }

    private Reservation createReservation(User reader, Book book, ReservationStatus status, Integer queuePosition) {
        return Reservation.builder()
                .reader(reader)
                .book(book)
                .reserveDate(LocalDate.now())
                .status(status)
                .queuePosition(queuePosition)
                .build();
    }
}
