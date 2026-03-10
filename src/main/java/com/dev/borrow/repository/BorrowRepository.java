package com.dev.borrow.repository;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    long countByUser_IdAndStatus(Long userId, BorrowStatus status);

    boolean existsByUser_IdAndStatus(Long userId, BorrowStatus status);

    List<Borrow> findByUser_Id(Long userId);

    List<Borrow> findByStatus(BorrowStatus status);

    long countByBook_IdAndStatus(Long bookId, BorrowStatus status);

    long countByStatus(BorrowStatus status);
}