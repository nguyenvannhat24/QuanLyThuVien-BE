package com.dev.book.service;

import com.dev.book.dto.AuthorRequest;
import com.dev.book.dto.AuthorResponse;
import com.dev.book.model.Author;
import com.dev.book.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    public AuthorResponse create(AuthorRequest request) {
        Author author = Author.builder()
                .authorName(request.getAuthorName())
                .biography(request.getBiography())
                .build();

        authorRepository.save(author);
        return mapToResponse(author);
    }

    @Override
    public List<AuthorResponse> getAll() {
        return authorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorResponse getById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        return mapToResponse(author);
    }

    @Override
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        author.setAuthorName(request.getAuthorName());
        author.setBiography(request.getBiography());

        authorRepository.save(author);
        return mapToResponse(author);
    }

    @Override
    public void delete(Long id) {
        authorRepository.deleteById(id);
    }

    private AuthorResponse mapToResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .authorName(author.getAuthorName())
                .biography(author.getBiography())
                .createdAt(author.getCreatedAt())
                .build();
    }
}
