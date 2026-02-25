package com.dev.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Username không được để trống")
    private String username;

    private String password;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private Gender gender;    
    private int age;
}
