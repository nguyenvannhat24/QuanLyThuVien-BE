package com.dev.auth.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    private String id;

    private String title;
    private String author;
    private String isbn;
    private String category;
    private int quantity;
    private boolean available;
    private double price;
}