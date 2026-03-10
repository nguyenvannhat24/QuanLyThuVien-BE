package com.dev.penalty.dto;

import com.dev.penalty.model.PenaltyType;
import com.dev.penalty.model.PenaltyStatus;
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
public class PenaltyResponse {
    private Long penaltyId;
    private Long borrowId;
    private Long readerId;
    private String readerName;
    private PenaltyType type;
    private BigDecimal amount;
    private PenaltyStatus status;
    private LocalDate createdDate;
    private LocalDate paidDate;
    private String notes;
}
