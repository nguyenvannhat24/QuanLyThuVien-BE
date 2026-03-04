package com.dev.borrow.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "borrows")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Borrow {

    @Id
    private String id;

    private String userId;
    private String bookId;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    private BorrowStatus status;

    private Integer extendCount;

    private Long fineAmount;
}