package com.dev.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookCopyRequest {

    @NotNull
    private Long bookId;

    @NotBlank
    @Size(min = 1, max = 100)
    private String copyCode;

    private String status;
    private String notes;
}
