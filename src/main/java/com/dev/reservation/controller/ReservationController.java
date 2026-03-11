package com.dev.reservation.controller;

import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import com.dev.reservation.dto.ReservationRequest;
import com.dev.reservation.dto.ReservationResponse;
import com.dev.reservation.service.ReservationService;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Đặt chỗ", description = "API quản lý đặt chỗ sách - tạo, xem và hủy đặt chỗ")
public class ReservationController {
    private final ReservationService reservationService;
    private final UserRepository userRepository;
    
    @PostMapping
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ReservationRequest request) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ReservationResponse response = reservationService.createReservation(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.RESERVATION_SUCCESS, response));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ReservationResponse> responses = reservationService.getMyReservations(user.getId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, responses));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        reservationService.cancelReservation(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.RESERVATION_CANCELLED, null));
    }
}
