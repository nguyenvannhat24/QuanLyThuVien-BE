package com.dev.book.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {

    private String id;
    private String title;
    private String author;
    private String category;
    private Double price;
    private Integer quantity;
}