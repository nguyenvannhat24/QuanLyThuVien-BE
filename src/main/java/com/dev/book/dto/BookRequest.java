package com.dev.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookRequest {

    @NotBlank
    private String title;

    private Long authorId;

    private Long categoryId;

    @NotNull
    private BigDecimal price;

    private String isbn;

    private Long publisherId;

    private Integer publishYear;

    private String description;
}