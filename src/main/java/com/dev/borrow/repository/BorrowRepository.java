package com.dev.borrow.repository;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    long countByUser_IdAndStatus(Long userId, BorrowStatus status);

    boolean existsByUser_IdAndStatus(Long userId, BorrowStatus status);

    List<Borrow> findByUser_Id(Long userId);

    @Query("SELECT b FROM Borrow b JOIN FETCH b.user JOIN FETCH b.bookCopy bc JOIN FETCH bc.book WHERE b.user.id = :userId")
    List<Borrow> findByUser_IdWithDetails(@Param("userId") Long userId);

    @Query("SELECT b FROM Borrow b JOIN FETCH b.user JOIN FETCH b.bookCopy bc JOIN FETCH bc.book")
    List<Borrow> findAllWithDetails();

    List<Borrow> findByStatus(BorrowStatus status);

    long countByBook_IdAndStatus(Long bookId, BorrowStatus status);

    long countByStatus(BorrowStatus status);

    List<Borrow> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);
    
    @Query("SELECT b FROM Borrow b JOIN FETCH b.user JOIN FETCH b.bookCopy bc JOIN FETCH bc.book WHERE b.status = :status AND b.dueDate < :date")
    List<Borrow> findByStatusAndDueDateBeforeWithDetails(@Param("status") BorrowStatus status, @Param("date") LocalDate date);
    
    long countByBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT bc.book.id, bc.book.title, bc.book.isbn, COUNT(b) " +
           "FROM Borrow b JOIN b.bookCopy bc " +
           "GROUP BY bc.book.id, bc.book.title, bc.book.isbn " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findTopBorrowedBooks(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT FUNCTION('YEAR', b.borrowDate), FUNCTION('MONTH', b.borrowDate), COUNT(b) " +
           "FROM Borrow b " +
           "WHERE b.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', b.borrowDate), FUNCTION('MONTH', b.borrowDate) " +
           "ORDER BY FUNCTION('YEAR', b.borrowDate), FUNCTION('MONTH', b.borrowDate)")
    List<Object[]> findBorrowingTrends(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT b FROM Borrow b JOIN FETCH b.user JOIN FETCH b.bookCopy bc JOIN FETCH bc.book WHERE b.borrowDate BETWEEN :startDate AND :endDate")
    List<Borrow> findByBorrowDateBetweenWithDetails(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}