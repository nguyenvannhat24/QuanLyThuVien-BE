package com.dev.book.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublisherResponse {

    private Long id;
    private String publisherName;
    private String address;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
}
