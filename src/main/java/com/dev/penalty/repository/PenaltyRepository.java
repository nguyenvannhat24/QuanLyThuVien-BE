package com.dev.penalty.repository;

import com.dev.penalty.model.Penalty;
import com.dev.penalty.model.PenaltyStatus;
import com.dev.user.model.User;
import com.dev.borrow.model.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    long countByReaderAndStatus(User reader, PenaltyStatus status);

    List<Penalty> findByReaderOrderByCreatedDateDesc(User reader);
    
    @Query("SELECT p FROM Penalty p LEFT JOIN FETCH p.borrowRecord LEFT JOIN FETCH p.reader WHERE p.reader = :reader ORDER BY p.createdDate DESC")
    List<Penalty> findByReaderOrderByCreatedDateDescWithDetails(@Param("reader") User reader);

    List<Penalty> findByStatus(PenaltyStatus status);

    List<Penalty> findByBorrowRecord(Borrow borrow);
    
    @Query("SELECT SUM(p.amount) FROM Penalty p WHERE p.status = :status")
    java.math.BigDecimal sumAmountByStatus(@Param("status") PenaltyStatus status);
    
    @Query("SELECT p FROM Penalty p LEFT JOIN FETCH p.borrowRecord b LEFT JOIN FETCH b.bookCopy bc LEFT JOIN FETCH bc.book LEFT JOIN FETCH p.reader WHERE p.status = :status")
    List<Penalty> findByStatusWithDetails(@Param("status") PenaltyStatus status);
    
    @Query("SELECT p FROM Penalty p LEFT JOIN FETCH p.borrowRecord b LEFT JOIN FETCH b.bookCopy bc LEFT JOIN FETCH bc.book LEFT JOIN FETCH p.reader")
    List<Penalty> findAllWithDetails();
}
