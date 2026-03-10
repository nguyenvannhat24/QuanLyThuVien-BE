package com.dev.borrow.controller;

import com.dev.borrow.dto.BorrowRequest;
import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;
import com.dev.borrow.service.BorrowService;
import com.dev.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final UserRepository userRepository;

    @PostMapping
    public BorrowResponse borrowBook(@RequestBody BorrowRequest request,
                                     Principal principal) {

        com.dev.user.model.User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return borrowService.borrowBook(user.getId(), request.getBookId());
    }

    @PutMapping("/{id}/return")
    public void returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
    }

    @PutMapping("/{id}/extend")
    public void extendBorrow(@PathVariable Long id) {
        borrowService.extendBorrow(id);
    }

    @GetMapping("/my-books")
    public List<BorrowResponse> myBooks(Principal principal) {
        com.dev.user.model.User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return borrowService.getMyBorrows(user.getId());
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