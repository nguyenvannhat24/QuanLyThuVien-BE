package com.dev.admin.service;

import com.dev.auth.model.Role;
import com.dev.user.model.User;
import com.dev.user.model.UserStatus;
import com.dev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Library@123";

    @Override
    @Transactional
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Không thể khóa tài khoản admin");
        }
        
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        
        Role role;
        try {
            role = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + newRole + ". Các vai trò hợp lệ: USER, LIBRARIAN, ADMIN");
        }
        
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);
    }
}
