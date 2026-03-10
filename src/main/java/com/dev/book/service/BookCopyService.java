package com.dev.book.service;

import com.dev.book.dto.BookCopyRequest;
import com.dev.book.dto.BookCopyResponse;

import java.util.List;

public interface BookCopyService {

    BookCopyResponse create(BookCopyRequest request);

    List<BookCopyResponse> getAll();

    BookCopyResponse getById(Long id);

    BookCopyResponse update(Long id, BookCopyRequest request);

    void delete(Long id);
}
