package com.dev.book.controller;

import com.dev.auth.controller.*;
import com.dev.book.dto.BookRequest;
import com.dev.book.dto.BookResponse;
import com.dev.book.service.BookService;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;

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
    public ResponseEntity<ApiResponse<BookResponse>> create(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.create(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.BOOK_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "title") String sortBy
    ) {
        Page<BookResponse> response = bookService.getAll(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getById(@PathVariable Long id) {
        BookResponse response = bookService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> update(@PathVariable Long id,
                           @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.BOOK_UPDATED, response));
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.BOOK_DELETED, "OK"));
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @RequestParam @jakarta.validation.constraints.NotBlank String keyword,
            Pageable pageable
    ) {
        Page<BookResponse> response = bookService.searchBooks(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/filter/author/{authorId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> filterByAuthor(
            @PathVariable Long authorId,
            Pageable pageable
    ) {
        Page<BookResponse> response = bookService.filterByAuthor(authorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/filter/category")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> filterByCategory(
            @RequestParam @jakarta.validation.constraints.NotBlank String name,
            Pageable pageable
    ) {
        Page<BookResponse> response = bookService.filterByCategory(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/filter/publisher/{publisherId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> filterByPublisher(
            @PathVariable Long publisherId,
            Pageable pageable
    ) {
        Page<BookResponse> response = bookService.filterByPublisher(publisherId, pageable);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/filter/year/{year}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> filterByPublishYear(
            @PathVariable Integer year,
            Pageable pageable
    ) {
        Page<BookResponse> response = bookService.filterByPublishYear(year, pageable);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/advanced-search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> advancedSearch(
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
        
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, result));
    }
}