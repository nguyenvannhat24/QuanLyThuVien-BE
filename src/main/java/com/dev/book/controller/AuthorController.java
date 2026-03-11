package com.dev.book.controller;

import com.dev.book.dto.AuthorRequest;
import com.dev.book.dto.AuthorResponse;
import com.dev.book.service.AuthorService;
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
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Tác giả", description = "API quản lý tác giả - tạo, xem, cập nhật, xóa tác giả")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<AuthorResponse>>> getAll() {
        List<AuthorResponse> authors = authorService.getAll();
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, authors));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<AuthorResponse>> getById(@PathVariable Long id) {
        AuthorResponse author = authorService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, author));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<AuthorResponse>> create(@Valid @RequestBody AuthorRequest request) {
        AuthorResponse author = authorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.AUTHOR_CREATED, author));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<AuthorResponse>> update(@PathVariable Long id,
                                                 @Valid @RequestBody AuthorRequest request) {
        AuthorResponse author = authorService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.AUTHOR_UPDATED, author));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.AUTHOR_DELETED, null));
    }
}
