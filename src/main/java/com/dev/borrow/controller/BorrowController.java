package com.dev.borrow.controller;

import com.dev.borrow.dto.BorrowRequest;
import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;
import com.dev.borrow.service.BorrowService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    public BorrowResponse borrowBook(@RequestBody BorrowRequest request,
                                     Principal principal) {

        String userId = principal.getName(); 
        return borrowService.borrowBook(userId, request.getBookId());
    }

    @PutMapping("/{id}/return")
    public void returnBook(@PathVariable String id) {
        borrowService.returnBook(id);
    }

    @PutMapping("/{id}/extend")
    public void extendBorrow(@PathVariable String id) {
        borrowService.extendBorrow(id);
    }

    @GetMapping("/my-books")
    public List<BorrowResponse> myBooks(Principal principal) {
        return borrowService.getMyBorrows(principal.getName());
    }

    @GetMapping
    public List<BorrowResponse> getAll() {
        return borrowService.getAll();
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return borrowService.getDashboard();
    }
}