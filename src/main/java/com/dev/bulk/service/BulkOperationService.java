package com.dev.bulk.service;

import com.dev.book.model.BookCopy;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BulkOperationService {
    
    Map<String, Object> importBooksFromCsv(MultipartFile file);
    
    List<BookCopy> generateBookCopies(Long bookId, int count, String startingCopyCode);
}
