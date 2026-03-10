package com.dev.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublisherRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    private String publisherName;

    private String address;
    private String phone;
    private String email;
}
