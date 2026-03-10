package com.dev.penalty.dto;

import com.dev.penalty.model.PenaltyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyRequest {
    private Long borrowId;
    private PenaltyType type;
    private BigDecimal amount;
    private String notes;
}
