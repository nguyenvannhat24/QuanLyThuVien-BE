package com.dev.penalty.controller;

import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import com.dev.penalty.dto.PenaltyRequest;
import com.dev.penalty.dto.PenaltyResponse;
import com.dev.penalty.service.PenaltyService;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
@Tag(name = "Phạt", description = "API quản lý phạt - tạo phạt, xem phạt và thanh toán phạt")
public class PenaltyController {
    private final PenaltyService penaltyService;
    private final UserRepository userRepository;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PenaltyResponse>> createManualPenalty(
            @RequestBody @Valid PenaltyRequest request) {
        PenaltyResponse response = penaltyService.createManualPenalty(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PENALTY_CREATED, response));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getMyPenalties(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<PenaltyResponse> responses = penaltyService.getMyPenalties(user.getId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, responses));
    }
    
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PenaltyResponse>> payPenalty(@PathVariable Long id) {
        PenaltyResponse response = penaltyService.payPenalty(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PENALTY_PAID, response));
    }
}
