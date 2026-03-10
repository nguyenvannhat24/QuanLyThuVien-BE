package com.dev.book.controller;

import com.dev.book.dto.BookCopyRequest;
import com.dev.book.dto.BookCopyResponse;
import com.dev.book.service.BookCopyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
@RequiredArgsConstructor
public class BookCopyController {

    private final BookCopyService bookCopyService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<BookCopyResponse>> getAll() {
        return ResponseEntity.ok(bookCopyService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BookCopyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookCopyService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BookCopyResponse> create(@Valid @RequestBody BookCopyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookCopyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BookCopyResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody BookCopyRequest request) {
        return ResponseEntity.ok(bookCopyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookCopyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
