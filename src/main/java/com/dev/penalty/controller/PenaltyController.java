package com.dev.penalty.controller;

import com.dev.penalty.dto.PenaltyRequest;
import com.dev.penalty.dto.PenaltyResponse;
import com.dev.penalty.service.PenaltyService;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
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
public class PenaltyController {
    private final PenaltyService penaltyService;
    private final UserRepository userRepository;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<PenaltyResponse> createManualPenalty(
            @RequestBody @Valid PenaltyRequest request) {
        return ResponseEntity.ok(penaltyService.createManualPenalty(request));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<List<PenaltyResponse>> getMyPenalties(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(penaltyService.getMyPenalties(user.getId()));
    }
    
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<PenaltyResponse> payPenalty(@PathVariable Long id) {
        return ResponseEntity.ok(penaltyService.payPenalty(id));
    }
}
