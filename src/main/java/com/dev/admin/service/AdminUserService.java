package com.dev.admin.service;

public interface AdminUserService {
    
    void lockUser(Long userId);
    
    void unlockUser(Long userId);
    
    void changeUserRole(Long userId, String newRole);
    
    void resetPassword(Long userId);
}
