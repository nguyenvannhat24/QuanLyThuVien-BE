package com.dev.auth.controller;

import com.dev.auth.dto.UpdateRoleRequest;
import com.dev.auth.model.User;
import com.dev.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String id,
            @RequestBody UpdateRoleRequest request,
             Authentication authentication
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
if (user.getUsername().equals(authentication.getName())) {
    return ResponseEntity.badRequest().body("You cannot change your own role");
}
        user.setRole(request.getRole());

        userRepository.save(user);

        return ResponseEntity.ok("Role updated successfully");
    }
}