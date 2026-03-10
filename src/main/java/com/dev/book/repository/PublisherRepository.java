package com.dev.book.repository;

import com.dev.book.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    
    Optional<Publisher> findByPublisherName(String publisherName);
    
    boolean existsByPublisherName(String publisherName);
}
