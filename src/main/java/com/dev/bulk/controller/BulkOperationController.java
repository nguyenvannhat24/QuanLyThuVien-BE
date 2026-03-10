package com.dev.bulk.controller;

import com.dev.audit.annotation.AdminAction;
import com.dev.bulk.dto.GenerateBookCopiesRequest;
import com.dev.bulk.service.BulkOperationService;
import com.dev.book.model.BookCopy;
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
    public ResponseEntity<Map<String, Object>> importBooks(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "File is empty");
            return ResponseEntity.badRequest().body(error);
        }
        
        long maxSizeBytes = 10 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "File size exceeds maximum allowed size of 10MB");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "File must be a CSV file");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> result = bulkOperationService.importBooksFromCsv(file);
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/book-copies/generate")
    @AdminAction("GENERATE_BOOK_COPIES")
    public ResponseEntity<Map<String, Object>> generateBookCopies(
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
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
