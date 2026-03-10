package com.dev.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthorRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    private String authorName;

    private String biography;
}
