package com.dev.book.controller;

import com.dev.book.dto.PublisherRequest;
import com.dev.book.dto.PublisherResponse;
import com.dev.book.service.PublisherService;
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
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
@Tag(name = "Nhà xuất bản", description = "API quản lý nhà xuất bản - tạo, xem, cập nhật, xóa nhà xuất bản")
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<PublisherResponse>>> getAll() {
        List<PublisherResponse> publishers = publisherService.getAll();
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, publishers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PublisherResponse>> getById(@PathVariable Long id) {
        PublisherResponse publisher = publisherService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, publisher));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PublisherResponse>> create(@Valid @RequestBody PublisherRequest request) {
        PublisherResponse publisher = publisherService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.PUBLISHER_CREATED, publisher));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PublisherResponse>> update(@PathVariable Long id,
                                                    @Valid @RequestBody PublisherRequest request) {
        PublisherResponse publisher = publisherService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PUBLISHER_UPDATED, publisher));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        publisherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PUBLISHER_DELETED, null));
    }
}
