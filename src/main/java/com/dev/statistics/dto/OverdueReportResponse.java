package com.dev.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverdueReportResponse {
    private Long borrowId;
    private Long readerId;
    private String readerName;
    private String readerEmail;
    private Long bookCopyId;
    private String bookTitle;
    private String isbn;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private Integer daysOverdue;
}
