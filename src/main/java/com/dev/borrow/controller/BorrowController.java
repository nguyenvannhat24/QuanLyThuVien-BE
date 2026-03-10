package com.dev.borrow.controller;

import com.dev.borrow.dto.BorrowRequest;
import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;
import com.dev.borrow.service.BorrowService;
import com.dev.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@Tag(name = "Mượn sách", description = "API quản lý mượn trả sách - mượn, trả, gia hạn và xem lịch sử")
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

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BorrowResponse> returnBorrow(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.returnBorrow(id));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BorrowResponse> renewBorrow(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.renewBorrow(id));
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