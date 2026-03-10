package com.dev.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyReportResponse {
    private Long penaltyId;
    private String penaltyType;
    private BigDecimal amount;
    private String status;
    private Long readerId;
    private String readerName;
    private LocalDate createdDate;
    private LocalDate paidDate;
}
