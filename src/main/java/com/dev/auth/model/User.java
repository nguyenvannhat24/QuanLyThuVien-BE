package com.dev.auth.model;


import com.dev.auth.model.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Document(collection = "users")
@Data
public class User  {
   @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank
    private String username;

    @Indexed(unique = true)
    @Email
    private String email;

    @NotBlank
    private String password;

    private Role role = Role.USER;

    private boolean enabled = true;

    private Gender gender;
    private int age;
}
