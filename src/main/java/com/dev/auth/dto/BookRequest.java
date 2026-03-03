package com.dev.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    private String category;

    @NotNull
    private Double price;

    @NotNull
    private Integer quantity;
}