package com.dev.borrow.service;

import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;

import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(String userId, String bookId);

    void returnBook(String borrowId);

    void extendBorrow(String borrowId);

    List<BorrowResponse> getMyBorrows(String userId);

    List<BorrowResponse> getAll();
    
    DashboardResponse getDashboard();
}