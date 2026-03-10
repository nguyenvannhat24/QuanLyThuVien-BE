package com.dev.book.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse {

    private Long id;
    private String categoryName;
    private String description;
    private LocalDateTime createdAt;
}
