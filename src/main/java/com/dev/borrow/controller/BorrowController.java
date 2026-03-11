package com.dev.borrow.controller;

import com.dev.borrow.dto.BorrowRequest;
import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;
import com.dev.borrow.service.BorrowService;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<BorrowResponse>> borrowBook(@RequestBody BorrowRequest request,
                                     Principal principal) {

        com.dev.user.model.User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        BorrowResponse response = borrowService.borrowBook(user.getId(), request.getBookId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.BORROW_SUCCESS, response));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BorrowResponse>> returnBorrow(@PathVariable Long id) {
        BorrowResponse response = borrowService.returnBorrow(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.RETURN_SUCCESS, response));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BorrowResponse>> renewBorrow(@PathVariable Long id) {
        BorrowResponse response = borrowService.renewBorrow(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.RENEW_SUCCESS, response));
    }

    @GetMapping("/my-books")
    public ResponseEntity<ApiResponse<List<BorrowResponse>>> myBooks(Principal principal) {
        com.dev.user.model.User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<BorrowResponse> response = borrowService.getMyBorrows(user.getId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BorrowResponse>>> getAll() {
        List<BorrowResponse> response = borrowService.getAll();
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        DashboardResponse response = borrowService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }
}