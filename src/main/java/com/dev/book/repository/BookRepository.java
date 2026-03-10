package com.dev.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dev.book.model.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthor_AuthorNameContainingIgnoreCase(String authorName, Pageable pageable);

    Page<Book> findByCategory_CategoryName(String categoryName, Pageable pageable);
    
    Page<Book> findByAuthor_Id(Long authorId, Pageable pageable);
    
    Page<Book> findByCategory_Id(Long categoryId, Pageable pageable);
    
    Page<Book> findByPublisher_Id(Long publisherId, Pageable pageable);
    
    Page<Book> findByPublishYear(Integer year, Pageable pageable);

    Optional<Book> findByIsbn(String isbn);
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher.publisherName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}