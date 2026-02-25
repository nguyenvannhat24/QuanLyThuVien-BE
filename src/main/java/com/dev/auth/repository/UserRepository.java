package com.dev.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dev.auth.model.User;
import java.util.List;


public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}