package com.dev.auth.service;

import com.dev.auth.dto.BookRequest;

import com.dev.auth.dto.BookResponse;
import com.dev.auth.model.Book;
import com.dev.auth.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public BookResponse create(BookRequest request) {

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        bookRepository.save(book);

        return mapToResponse(book);
    }

@Override
public Page<BookResponse> getAll(int page, int size, String sortBy) {

    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortBy).ascending()
    );

    Page<Book> bookPage = bookRepository.findAll(pageable);

    return bookPage.map(this::mapToResponse);
}

    @Override
    public BookResponse getById(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return mapToResponse(book);
    }

    @Override
    public BookResponse update(String id, BookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setPrice(request.getPrice());
        book.setQuantity(request.getQuantity());

        bookRepository.save(book);

        return mapToResponse(book);
    }

    @Override
    public void delete(String id) {
        bookRepository.deleteById(id);
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .build();
    }
}