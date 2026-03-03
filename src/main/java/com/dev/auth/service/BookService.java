package com.dev.auth.service;



import com.dev.auth.dto.BookRequest;
import com.dev.auth.dto.BookResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface BookService {

    BookResponse create(BookRequest request);

    Page<BookResponse> getAll(int page, int size, String sortBy);

    BookResponse getById(String id);

    BookResponse update(String id, BookRequest request);

    void delete(String id);
}
