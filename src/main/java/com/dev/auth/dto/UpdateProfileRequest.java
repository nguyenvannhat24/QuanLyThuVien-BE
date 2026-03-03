package com.dev.auth.dto;

import com.dev.auth.model.Gender;

import lombok.Data;


  @Data
public class UpdateProfileRequest {
    private String email;
    private Integer age;
    private Gender gender;
}

