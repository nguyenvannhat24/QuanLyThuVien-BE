package com.dev.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.dev.book.model.Book;

import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Book> findByCategory(String category, Pageable pageable);

    Page<Book> findByQuantityGreaterThan(int quantity, Pageable pageable);

}