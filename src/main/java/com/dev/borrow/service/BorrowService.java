package com.dev.borrow.service;

import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;

import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(Long userId, Long bookId);

    void returnBook(Long borrowId);

    void extendBorrow(Long borrowId);

    List<BorrowResponse> getMyBorrows(Long userId);

    List<BorrowResponse> getAll();
    
    DashboardResponse getDashboard();
}