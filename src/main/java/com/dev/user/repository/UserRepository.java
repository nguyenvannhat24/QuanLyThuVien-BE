package com.dev.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dev.user.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void deleteById(String id);

}