package com.dev.book.service;



import com.dev.book.dto.BookRequest;
import com.dev.book.dto.BookResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookService {

    BookResponse create(BookRequest request);

    Page<BookResponse> getAll(int page, int size, String sortBy);

    BookResponse getById(Long id);

    BookResponse update(Long id, BookRequest request);

    void delete(Long id);

    Page<BookResponse> searchBooks(String keyword, Pageable pageable);

    Page<BookResponse> filterByAuthor(Long authorId, Pageable pageable);

    Page<BookResponse> filterByCategory(String categoryName, Pageable pageable);

    Page<BookResponse> filterByPublisher(Long publisherId, Pageable pageable);

    Page<BookResponse> filterByPublishYear(Integer year, Pageable pageable);
}
