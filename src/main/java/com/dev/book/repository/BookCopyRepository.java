package com.dev.book.repository;

import com.dev.book.model.Book;
import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    
    List<BookCopy> findByBook(Book book);
    
    List<BookCopy> findByBook_Id(Long bookId);
    
    List<BookCopy> findByStatus(BookCopyStatus status);
    
    List<BookCopy> findByBook_IdAndStatus(Long bookId, BookCopyStatus status);
    
    Optional<BookCopy> findByCopyCode(String copyCode);
    
    boolean existsByCopyCode(String copyCode);
    
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId AND bc.status = :status")
    long countByBookIdAndStatus(Long bookId, BookCopyStatus status);
    
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId AND bc.status = 'AVAILABLE'")
    long countAvailableCopiesByBookId(Long bookId);
    
    long countByBookAndStatus(Book book, BookCopyStatus status);
}
