package com.dev.book.specification;

import com.dev.book.model.Book;
import com.dev.book.model.Author;
import com.dev.book.model.Category;
import com.dev.book.model.Publisher;
import com.dev.book.model.BookCopy;
import com.dev.book.model.BookCopyStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * BookSpecification provides static methods to build JPA Specifications for Book entity queries.
 * This utility class enables dynamic query composition for advanced book search functionality.
 */
public class BookSpecification {

    /**
     * Creates a specification to search books by title (case-insensitive LIKE).
     * 
     * @param keyword The search keyword to match against book titles
     * @return Specification for title search, or null if keyword is null
     */
    public static Specification<Book> titleContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), 
                "%" + keyword.toLowerCase() + "%"
            );
    }

    /**
     * Creates a specification to search books by author name (case-insensitive LIKE).
     * Uses LEFT JOIN to Author entity to avoid N+1 queries.
     * 
     * @param keyword The search keyword to match against author names
     * @return Specification for author name search, or null if keyword is null
     */
    public static Specification<Book> authorNameContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<Book, Author> authorJoin = root.join("author", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(authorJoin.get("authorName")), 
                "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    /**
     * Creates a specification to search books by ISBN (exact match).
     * 
     * @param isbn The ISBN to match
     * @return Specification for ISBN search, or null if isbn is null
     */
    public static Specification<Book> isbnEquals(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("isbn"), isbn);
    }

    /**
     * Creates a specification to filter books by category ID.
     * Uses LEFT JOIN to Category entity to avoid N+1 queries.
     * 
     * @param categoryId The category ID to filter by
     * @return Specification for category filter, or null if categoryId is null
     */
    public static Specification<Book> hasCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<Book, Category> categoryJoin = root.join("category", JoinType.LEFT);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    /**
     * Creates a specification to filter books by publisher ID.
     * Uses LEFT JOIN to Publisher entity to avoid N+1 queries.
     * 
     * @param publisherId The publisher ID to filter by
     * @return Specification for publisher filter, or null if publisherId is null
     */
    public static Specification<Book> hasPublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<Book, Publisher> publisherJoin = root.join("publisher", JoinType.LEFT);
            return criteriaBuilder.equal(publisherJoin.get("id"), publisherId);
        };
    }

    /**
     * Creates a specification to filter books that have at least one available copy.
     * Uses a subquery to check for available BookCopy entities without causing N+1 queries.
     * 
     * @return Specification for available books filter
     */
    public static Specification<Book> hasAvailableCopies() {
        return (root, query, criteriaBuilder) -> {
            // Create subquery to check if book has available copies
            Subquery<Long> subquery = query.subquery(Long.class);
            var subRoot = subquery.from(BookCopy.class);
            subquery.select(subRoot.get("book").get("id"))
                    .where(
                        criteriaBuilder.and(
                            criteriaBuilder.equal(subRoot.get("book").get("id"), root.get("id")),
                            criteriaBuilder.equal(subRoot.get("status"), BookCopyStatus.AVAILABLE)
                        )
                    );
            
            return criteriaBuilder.exists(subquery);
        };
    }
}
