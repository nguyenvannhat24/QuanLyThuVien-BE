package com.dev.book.controller;

import com.dev.book.dto.BookCopyRequest;
import com.dev.book.dto.BookCopyResponse;
import com.dev.book.service.BookCopyService;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bản sao sách", description = "API quản lý bản sao sách - tạo, xem, cập nhật, xóa bản sao")
public class BookCopyController {

    private final BookCopyService bookCopyService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<BookCopyResponse>>> getAll() {
        List<BookCopyResponse> bookCopies = bookCopyService.getAll();
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, bookCopies));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<BookCopyResponse>> getById(@PathVariable Long id) {
        BookCopyResponse bookCopy = bookCopyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, bookCopy));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookCopyResponse>> create(@Valid @RequestBody BookCopyRequest request) {
        BookCopyResponse bookCopy = bookCopyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, bookCopy));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookCopyResponse>> update(@PathVariable Long id,
                                                   @Valid @RequestBody BookCopyRequest request) {
        BookCopyResponse bookCopy = bookCopyService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, bookCopy));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bookCopyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, null));
    }
}
