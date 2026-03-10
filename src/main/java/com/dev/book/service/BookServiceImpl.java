package com.dev.book.service;

import com.dev.book.dto.BookRequest;
import com.dev.book.dto.BookResponse;
import com.dev.book.dto.AuthorResponse;
import com.dev.book.dto.CategoryResponse;
import com.dev.book.dto.PublisherResponse;
import com.dev.book.model.Book;
import com.dev.book.model.Author;
import com.dev.book.model.Category;
import com.dev.book.model.Publisher;
import com.dev.book.repository.BookRepository;
import com.dev.book.repository.AuthorRepository;
import com.dev.book.repository.CategoryRepository;
import com.dev.book.repository.PublisherRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    @Override
    public BookResponse create(BookRequest request) {

        Author author = null;
        if (request.getAuthorId() != null) {
            author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("Author not found"));
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        Publisher publisher = null;
        if (request.getPublisherId() != null) {
            publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found"));
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(author)
                .isbn(request.getIsbn())
                .category(category)
                .publisher(publisher)
                .price(request.getPrice())
                .publishYear(request.getPublishYear())
                .description(request.getDescription())
                .build();

        bookRepository.save(book);

        return mapToResponse(book);
    }

    @Override
    public Page<BookResponse> getAll(int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).ascending());

        Page<Book> bookPage = bookRepository.findAll(pageable);

        return bookPage.map(this::mapToResponse);
    }

    @Override
    public BookResponse getById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return mapToResponse(book);
    }

    @Override
    public BookResponse update(Long id, BookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Author author = null;
        if (request.getAuthorId() != null) {
            author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("Author not found"));
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        Publisher publisher = null;
        if (request.getPublisherId() != null) {
            publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found"));
        }

        book.setTitle(request.getTitle());
        book.setAuthor(author);
        book.setIsbn(request.getIsbn());
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setPrice(request.getPrice());
        book.setPublishYear(request.getPublishYear());
        book.setDescription(request.getDescription());

        bookRepository.save(book);

        return mapToResponse(book);
    }

    @Override
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(mapAuthorToResponse(book.getAuthor()))
                .isbn(book.getIsbn())
                .category(mapCategoryToResponse(book.getCategory()))
                .publisher(mapPublisherToResponse(book.getPublisher()))
                .price(book.getPrice())
                .publishYear(book.getPublishYear())
                .description(book.getDescription())
                .build();
    }

    private AuthorResponse mapAuthorToResponse(Author author) {
        if (author == null) return null;
        return AuthorResponse.builder()
                .id(author.getId())
                .authorName(author.getAuthorName())
                .biography(author.getBiography())
                .createdAt(author.getCreatedAt())
                .build();
    }

    private CategoryResponse mapCategoryToResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private PublisherResponse mapPublisherToResponse(Publisher publisher) {
        if (publisher == null) return null;
        return PublisherResponse.builder()
                .id(publisher.getId())
                .publisherName(publisher.getPublisherName())
                .address(publisher.getAddress())
                .phone(publisher.getPhone())
                .email(publisher.getEmail())
                .createdAt(publisher.getCreatedAt())
                .build();
    }
}