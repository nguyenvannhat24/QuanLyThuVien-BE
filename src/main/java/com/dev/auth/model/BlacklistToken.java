package com.dev.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "blacklist_tokens")
public class BlacklistToken {

    @Id
    private String id;

    private String token;

    private Date expiryDate;
}