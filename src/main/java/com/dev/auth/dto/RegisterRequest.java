package com.dev.auth.dto;

import com.dev.auth.model.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;

    @Email
    private String email;

    private Gender gender;
    private int age;
    
}
