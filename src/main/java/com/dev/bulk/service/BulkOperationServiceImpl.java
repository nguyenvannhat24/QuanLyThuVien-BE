package com.dev.bulk.service;

import com.dev.book.model.*;
import com.dev.book.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BulkOperationServiceImpl implements BulkOperationService {
    
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    
    private String sanitizeCsvField(String field) {
        if (field == null) return "";
        String trimmed = field.trim();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || 
            trimmed.startsWith("-") || trimmed.startsWith("@")) {
            return "'" + trimmed;
        }
        return trimmed;
    }
    
    @Override
    @Transactional
    public Map<String, Object> importBooksFromCsv(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (lineNumber == 1 && line.toLowerCase().contains("title")) {
                    continue;
                }
                
                try {
                    String[] fields = parseCSVLine(line);
                    
                    if (fields.length < 7) {
                        errors.add("Line " + lineNumber + ": Invalid format - expected 7 fields");
                        errorCount++;
                        continue;
                    }
                    
                    String title = sanitizeCsvField(fields[0]);
                    String isbn = sanitizeCsvField(fields[1]);
                    String authorName = sanitizeCsvField(fields[2]);
                    String categoryName = sanitizeCsvField(fields[3]);
                    String publisherName = sanitizeCsvField(fields[4]);
                    String publicationYearStr = sanitizeCsvField(fields[5]);
                    String totalCopiesStr = sanitizeCsvField(fields[6]);
                    
                    if (title.isEmpty() || authorName.isEmpty()) {
                        errors.add("Line " + lineNumber + ": Title and author are required");
                        errorCount++;
                        continue;
                    }
                    
                    if (!isbn.isEmpty() && bookRepository.findByIsbn(isbn).isPresent()) {
                        errors.add("Line " + lineNumber + ": Book with ISBN " + isbn + " already exists");
                        errorCount++;
                        continue;
                    }
                    
                    Author author = authorRepository.findByAuthorName(authorName)
                            .orElseGet(() -> authorRepository.save(
                                    Author.builder()
                                            .authorName(authorName)
                                            .build()));
                    
                    Category category = null;
                    if (!categoryName.isEmpty()) {
                        category = categoryRepository.findByCategoryName(categoryName)
                                .orElseGet(() -> categoryRepository.save(
                                        Category.builder()
                                                .categoryName(categoryName)
                                                .build()));
                    }
                    
                    Publisher publisher = null;
                    if (!publisherName.isEmpty()) {
                        publisher = publisherRepository.findByPublisherName(publisherName)
                                .orElseGet(() -> publisherRepository.save(
                                        Publisher.builder()
                                                .publisherName(publisherName)
                                                .build()));
                    }
                    
                    Integer publicationYear = null;
                    try {
                        if (!publicationYearStr.isEmpty()) {
                            publicationYear = Integer.parseInt(publicationYearStr);
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Line " + lineNumber + ": Invalid publication year format");
                        errorCount++;
                        continue;
                    }
                    
                    int totalCopies = 0;
                    try {
                        if (!totalCopiesStr.isEmpty()) {
                            totalCopies = Integer.parseInt(totalCopiesStr);
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Line " + lineNumber + ": Invalid total copies format");
                        errorCount++;
                        continue;
                    }
                    
                    Book book = Book.builder()
                            .title(title)
                            .isbn(isbn.isEmpty() ? null : isbn)
                            .author(author)
                            .category(category)
                            .publisher(publisher)
                            .publishYear(publicationYear)
                            .build();
                    
                    book = bookRepository.save(book);
                    
                    if (totalCopies > 0) {
                        String baseCode = "COPY-" + book.getId() + "-";
                        for (int i = 1; i <= totalCopies; i++) {
                            String copyCode = baseCode + String.format("%03d", i);
                            
                            if (!bookCopyRepository.existsByCopyCode(copyCode)) {
                                BookCopy bookCopy = BookCopy.builder()
                                        .book(book)
                                        .copyCode(copyCode)
                                        .status(BookCopyStatus.AVAILABLE)
                                        .build();
                                bookCopyRepository.save(bookCopy);
                            }
                        }
                    }
                    
                    successCount++;
                    
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                }
            }
            
        } catch (Exception e) {
            errors.add("Failed to read CSV file: " + e.getMessage());
            errorCount++;
        }
        
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        
        return result;
    }
    
    @Override
    @Transactional
    public List<BookCopy> generateBookCopies(Long bookId, int count, String startingCopyCode) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        List<BookCopy> createdCopies = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String copyCode = startingCopyCode + String.format("%03d", i + 1);
            
            if (bookCopyRepository.existsByCopyCode(copyCode)) {
                throw new RuntimeException("Copy code already exists: " + copyCode);
            }
            
            try {
                BookCopy bookCopy = BookCopy.builder()
                        .book(book)
                        .copyCode(copyCode)
                        .status(BookCopyStatus.AVAILABLE)
                        .build();
                
                bookCopy = bookCopyRepository.save(bookCopy);
                createdCopies.add(bookCopy);
            } catch (DataIntegrityViolationException e) {
                throw new RuntimeException("Copy code already exists (concurrent creation): " + copyCode);
            }
        }
        
        return createdCopies;
    }
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
}
