package com.dev.book.dto;

import com.dev.book.dto.AuthorResponse;
import com.dev.book.dto.CategoryResponse;
import com.dev.book.dto.PublisherResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private AuthorResponse author;
    private CategoryResponse category;
    private PublisherResponse publisher;
    private BigDecimal price;
    private String isbn;
    private Integer publishYear;
    private String description;
}