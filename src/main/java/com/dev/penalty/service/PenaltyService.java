package com.dev.penalty.service;

import com.dev.borrow.model.Borrow;
import com.dev.penalty.dto.PenaltyRequest;
import com.dev.penalty.dto.PenaltyResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PenaltyService {
    PenaltyResponse createOverduePenalty(Borrow borrow);
    
    PenaltyResponse createManualPenalty(PenaltyRequest request);
    
    PenaltyResponse payPenalty(Long penaltyId);
    
    List<PenaltyResponse> getMyPenalties(Long userId);
    
    long countUnpaidPenalties(Long userId);
    
    BigDecimal calculateOverdueFine(Borrow borrow, LocalDate returnDate);
}
