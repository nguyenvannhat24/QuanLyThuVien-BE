package com.dev.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dev.auth.model.BlacklistToken;

public interface BlacklistTokenRepository 
        extends MongoRepository<BlacklistToken, String> {

    boolean existsByToken(String token);
}