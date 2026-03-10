package com.dev.book.service;

import com.dev.book.dto.AuthorRequest;
import com.dev.book.dto.AuthorResponse;

import java.util.List;

public interface AuthorService {

    AuthorResponse create(AuthorRequest request);

    List<AuthorResponse> getAll();

    AuthorResponse getById(Long id);

    AuthorResponse update(Long id, AuthorRequest request);

    void delete(Long id);
}
