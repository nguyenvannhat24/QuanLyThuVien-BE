package com.dev.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.dev.auth.model.Book;



import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findByTitleContainingIgnoreCase(String title);

}