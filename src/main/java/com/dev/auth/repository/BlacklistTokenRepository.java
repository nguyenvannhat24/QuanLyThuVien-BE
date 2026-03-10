package com.dev.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.auth.model.BlacklistToken;

@Repository
public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, Long> {

    boolean existsByToken(String token);
}