package com.dev.book.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthorResponse {

    private Long id;
    private String authorName;
    private String biography;
    private LocalDateTime createdAt;
}
