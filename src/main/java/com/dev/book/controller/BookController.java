package com.dev.book.controller;

import com.dev.auth.controller.*;
import com.dev.book.dto.BookRequest;
import com.dev.book.dto.BookResponse;
import com.dev.book.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
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
    public BookResponse getById(@PathVariable String id) {
        return bookService.getById(id);
    }

    @PutMapping("/admin/update/{id}")
    public BookResponse update(@PathVariable String id,
                               @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/admin/delete/{id}")
    public void delete(@PathVariable String id) {
        bookService.delete(id);
    }
}