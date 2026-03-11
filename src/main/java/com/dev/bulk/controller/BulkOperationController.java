package com.dev.bulk.controller;

import com.dev.audit.annotation.AdminAction;
import com.dev.bulk.dto.GenerateBookCopiesRequest;
import com.dev.bulk.service.BulkOperationService;
import com.dev.book.model.BookCopy;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/bulk")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Thao tác hàng loạt", description = "API thao tác hàng loạt - import sách, tạo bản sao sách")
public class BulkOperationController {
    
    private final BulkOperationService bulkOperationService;
    
    @PostMapping("/books/import")
    @AdminAction("BULK_IMPORT_BOOKS")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importBooks(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }
        
        long maxSizeBytes = 10 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(ApiResponse.error("File size exceeds maximum allowed size of 10MB"));
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File must be a CSV file"));
        }
        
        Map<String, Object> result = bulkOperationService.importBooksFromCsv(file);
        
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, result));
    }
    
    @PostMapping("/book-copies/generate")
    @AdminAction("GENERATE_BOOK_COPIES")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateBookCopies(
            @RequestBody @Valid GenerateBookCopiesRequest request) {
        
        try {
            List<BookCopy> createdCopies = bulkOperationService.generateBookCopies(
                    request.getBookId(),
                    request.getCount(),
                    request.getStartingCopyCode()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book copies generated successfully");
            response.put("count", createdCopies.size());
            response.put("copyIds", createdCopies.stream()
                    .map(BookCopy::getId)
                    .collect(Collectors.toList()));
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Book copies generated successfully", response));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
