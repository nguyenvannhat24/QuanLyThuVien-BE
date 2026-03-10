package com.dev.borrow.service;

import com.dev.borrow.dto.BorrowResponse;
import com.dev.borrow.dto.DashboardResponse;

import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(Long userId, Long bookId);

    BorrowResponse returnBorrow(Long id);

    BorrowResponse renewBorrow(Long id);

    List<BorrowResponse> getMyBorrows(Long userId);

    List<BorrowResponse> getAll();
    
    DashboardResponse getDashboard();
}