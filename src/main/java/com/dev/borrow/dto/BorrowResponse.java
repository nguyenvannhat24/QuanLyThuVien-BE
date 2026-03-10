package com.dev.borrow.dto;

import com.dev.borrow.model.BorrowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BorrowResponse {

    private Long id;
    private Long userId;
    private Long bookId;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    private BorrowStatus status;
}