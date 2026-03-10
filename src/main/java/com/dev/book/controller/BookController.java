package com.dev.book.controller;

import com.dev.auth.controller.*;
import com.dev.book.dto.BookRequest;
import com.dev.book.dto.BookResponse;
import com.dev.book.service.BookService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Sách", description = "API quản lý sách - tạo, cập nhật, xóa, tìm kiếm và lọc sách")
public class BookController {

    private final BookService bookService;

    @PostMapping("/admin/create")
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        return bookService.create(request);
    }

 @GetMapping
public Page<BookResponse> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "title") String sortBy
) {
    return bookService.getAll(page, size, sortBy);
}

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @PutMapping("/admin/update/{id}")
    public BookResponse update(@PathVariable Long id,
                               @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/admin/delete/{id}")
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public Page<BookResponse> searchBooks(
            @RequestParam @jakarta.validation.constraints.NotBlank String keyword,
            Pageable pageable
    ) {
        return bookService.searchBooks(keyword, pageable);
    }

    @GetMapping("/filter/author/{authorId}")
    @PreAuthorize("permitAll()")
    public Page<BookResponse> filterByAuthor(
            @PathVariable Long authorId,
            Pageable pageable
    ) {
        return bookService.filterByAuthor(authorId, pageable);
    }

    @GetMapping("/filter/category")
    @PreAuthorize("permitAll()")
    public Page<BookResponse> filterByCategory(
            @RequestParam @jakarta.validation.constraints.NotBlank String name,
            Pageable pageable
    ) {
        return bookService.filterByCategory(name, pageable);
    }

    @GetMapping("/filter/publisher/{publisherId}")
    @PreAuthorize("permitAll()")
    public Page<BookResponse> filterByPublisher(
            @PathVariable Long publisherId,
            Pageable pageable
    ) {
        return bookService.filterByPublisher(publisherId, pageable);
    }

    @GetMapping("/filter/year/{year}")
    @PreAuthorize("permitAll()")
    public Page<BookResponse> filterByPublishYear(
            @PathVariable Integer year,
            Pageable pageable
    ) {
        return bookService.filterByPublishYear(year, pageable);
    }

    @GetMapping("/advanced-search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookResponse> result = bookService.advancedSearch(
                keyword, categoryId, publisherId, availableOnly, pageable
        );
        
        return ResponseEntity.ok(result);
    }
}