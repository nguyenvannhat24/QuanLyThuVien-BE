package com.dev.book.service;

import com.dev.book.dto.BookCopyRequest;
import com.dev.book.dto.BookCopyResponse;
import com.dev.book.model.Book;
import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import com.dev.book.repository.BookCopyRepository;
import com.dev.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    @Override
    public BookCopyResponse create(BookCopyRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BookCopyStatus status = BookCopyStatus.AVAILABLE;
        if (request.getStatus() != null) {
            try {
                status = BookCopyStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                status = BookCopyStatus.AVAILABLE;
            }
        }

        BookCopy bookCopy = BookCopy.builder()
                .book(book)
                .copyCode(request.getCopyCode())
                .status(status)
                .notes(request.getNotes())
                .build();

        bookCopyRepository.save(bookCopy);
        return mapToResponse(bookCopy);
    }

    @Override
    public List<BookCopyResponse> getAll() {
        return bookCopyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookCopyResponse getById(Long id) {
        BookCopy bookCopy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookCopy not found"));
        return mapToResponse(bookCopy);
    }

    @Override
    public BookCopyResponse update(Long id, BookCopyRequest request) {
        BookCopy bookCopy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookCopy not found"));

        if (request.getBookId() != null && !request.getBookId().equals(bookCopy.getBook().getId())) {
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            bookCopy.setBook(book);
        }

        bookCopy.setCopyCode(request.getCopyCode());
        
        if (request.getStatus() != null) {
            try {
                bookCopy.setStatus(BookCopyStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                bookCopy.setStatus(BookCopyStatus.AVAILABLE);
            }
        }

        bookCopy.setNotes(request.getNotes());

        bookCopyRepository.save(bookCopy);
        return mapToResponse(bookCopy);
    }

    @Override
    public void delete(Long id) {
        bookCopyRepository.deleteById(id);
    }

    private BookCopyResponse mapToResponse(BookCopy bookCopy) {
        return BookCopyResponse.builder()
                .id(bookCopy.getId())
                .bookId(bookCopy.getBook().getId())
                .copyCode(bookCopy.getCopyCode())
                .status(bookCopy.getStatus().name())
                .notes(bookCopy.getNotes())
                .createdAt(bookCopy.getCreatedAt())
                .updatedAt(bookCopy.getUpdatedAt())
                .build();
    }
}
