package com.dev.reservation.repository;

import com.dev.reservation.model.Reservation;
import com.dev.reservation.model.ReservationStatus;
import com.dev.book.model.Book;
import com.dev.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByBookAndStatusOrderByQueuePositionAsc(Book book, ReservationStatus status);

    List<Reservation> findByStatusAndExpireDateBefore(ReservationStatus status, LocalDate date);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.book WHERE r.status = :status AND r.expireDate < :date")
    List<Reservation> findByStatusAndExpireDateBeforeWithBook(@Param("status") ReservationStatus status, @Param("date") LocalDate date);

    List<Reservation> findByReaderOrderByReserveDateDesc(User reader);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.book JOIN FETCH r.reader WHERE r.reader = :user ORDER BY r.reserveDate DESC")
    List<Reservation> findByReaderOrderByReserveDateDescWithDetails(@Param("user") User user);
    
    boolean existsByReaderAndBookAndStatusIn(User reader, Book book, List<ReservationStatus> statuses);
    
    Optional<Reservation> findTopByBookAndStatusOrderByQueuePositionAsc(Book book, ReservationStatus status);

    int countByBookAndStatus(Book book, ReservationStatus status);
}
