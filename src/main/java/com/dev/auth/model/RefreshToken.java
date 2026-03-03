package com.dev.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    private String username;

    private String token;

    private Date expiryDate;
}