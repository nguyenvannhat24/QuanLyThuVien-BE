package com.dev.bulk.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateBookCopiesRequest {
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 100, message = "Count must not exceed 100")
    private Integer count;
    
    @NotBlank(message = "Starting copy code is required")
    private String startingCopyCode;
}
