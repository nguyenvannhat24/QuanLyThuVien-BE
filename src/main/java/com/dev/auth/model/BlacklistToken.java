package com.dev.auth.model;

import lombok.Data;
import jakarta.persistence.*;

import java.util.Date;

@Data
@Entity
@Table(name = "blacklist_tokens", indexes = {
    @Index(name = "idx_token", columnList = "token")
})
public class BlacklistToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 500, nullable = false)
    private String token;

    @Column(nullable = false)
    private Date expiryDate;
}