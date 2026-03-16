# AGENTS.md - Development Guidelines for Library Management System

## Project Overview
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: MySQL 8.0 (production), H2 (testing)
- **Port**: 8081 (default)

---

## Build & Development Commands

### Run Application
```bash
# Development mode with hot reload
mvn spring-boot:run

# Build and run
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing
```bash
# Run all tests
mvn test

# Run single test class
mvn test -Dtest=BorrowRepositoryTest

# Run single test method
mvn test -Dtest=BorrowRepositoryTest#testCountByUser_IdAndStatus

# Run tests with coverage (requires jacoco plugin)
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html

# Run specific profile tests
mvn test -Dspring.profiles.active=test
```

### Build Production
```bash
# Build JAR file
mvn clean package -DskipTests

# Build with Docker
docker compose up -d --build
```

### Database
```bash
# Run with docker-compose (MySQL + phpMyAdmin)
docker compose up -d mysql phpmyadmin

# Access phpMyAdmin: http://localhost:8082
# Credentials: library_user / library_pass

# View Swagger API docs: http://localhost:8081/swagger-ui.html
```

---

## Project Structure

```
src/main/java/com/dev/
├── auth/                    # Authentication & JWT
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   ├── security/
│   └── service/
├── book/                    # Book management
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── specification/
├── borrow/                 # Borrowing & returns
├── reservation/             # Book reservations
├── penalty/                # Penalties & payments
├── notification/            # Notifications
├── statistics/             # Reports & stats
├── user/                   # User management
├── config/                 # Configuration classes
├── constant/                # Constants
├── dto/                    # Shared DTOs
├── exception/              # Exception handling
└── DevApplication.java
```

---

## Code Style Guidelines

### General Conventions
- Use **Spring Boot standard patterns**: ServiceImpl suffix, Controller suffix
- Use **Lombok** to reduce boilerplate (@Data, @Builder, @RequiredArgsConstructor)
- Use **Vietnamese** in comments and user-facing messages
- Follow **SOLID principles** and clean architecture
- Always use **interfaces** for services (BookService, not BookServiceImpl in controllers)

### Naming Conventions
| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `BookServiceImpl` |
| Methods | camelCase | `getAllBooks()` |
| Variables | camelCase | `bookRepository` |
| Constants | UPPER_SNAKE_CASE | `MAX_BORROW_LIMIT` |
| Packages | lowercase | `com.dev.book` |
| DTOs | End with Request/Response | `BookRequest`, `BookResponse` |

### API Response Format
All API endpoints MUST return `ApiResponse<T>` wrapper:
```java
// Success response
return ResponseEntity.ok(ApiResponse.success(MessageConstants.BOOK_CREATED, response));

// Success with no data
return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS));

// Error response (via exception handler)
throw new ResourceNotFoundException(MessageConstants.BOOK_NOT_FOUND);
```

### Controller Guidelines
```java
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Sách", description = "API quản lý sách")
public class BookController {

    private final BookService bookService;

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> create(
            @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.create(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.BOOK_CREATED, response));
    }
}
```

- Use `@Valid` for request validation
- Use `@PreAuthorize` for role-based access
- Use `@Tag` and `@Operation` for Swagger documentation
- Use message constants from `MessageConstants` class

### Service Layer Guidelines
```java
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Override
    @Transactional
    public BookResponse create(BookRequest request) {
        // Validate existence of related entities
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.AUTHOR_NOT_FOUND));
        
        // Business logic
        Book book = Book.builder()
                .title(request.getTitle())
                .author(author)
                .build();
        
        return mapToResponse(bookRepository.save(book));
    }
}
```

- Always use `@Transactional` for write operations
- Use constructor injection via `@RequiredArgsConstructor`
- Throw specific exceptions (ResourceNotFoundException, BusinessException)
- Use `mapToResponse()` or mapper classes for entity-to-DTO conversion

### Repository Guidelines
```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    @Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.id = :id")
    Optional<Book> findByIdWithAuthor(@Param("id") Long id);
    
    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
```

- Use Spring Data JPA methods when possible
- Use `@Query` for complex queries with JPQL
- Use **JOIN FETCH** to prevent N+1 queries
- Use `@Param` for query parameters
- Return `Page<T>` for paginated results

### Model/Entity Guidelines
```java
@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;
}
```

- Use `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` from Lombok
- Use lazy loading (`FetchType.LAZY`) for relationships
- Use `@Column` for specific constraints
- Use enums for fixed values (status, type, etc.)

### DTO Guidelines
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookResponse {
    private Long id;
    private String title;
    private String isbn;
    private AuthorResponse author;
    private CategoryResponse category;
}
```

- Use `@JsonInclude(JsonInclude.Include.NON_NULL)` to exclude null fields
- Separate Request/Response DTOs
- Use nested DTOs for related entities (not entire entities)

### Validation
Use Bean Validation annotations:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9-]{10,17}$", message = "Invalid ISBN format")
    private String isbn;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Min(value = 1000, message = "Publish year must be after 1000")
    @Max(value = 2030, message = "Publish year must not exceed 2030")
    private Integer publishYear;
}
```

### Error Handling
Use the global exception handler (`GlobalExceptionHandler`) and custom exceptions:
```java
// Throw in service layer
throw new ResourceNotFoundException(MessageConstants.BOOK_NOT_FOUND);
throw new BusinessException(MessageConstants.BOOK_ALREADY_BORROWED);

// Custom exceptions are in: src/main/java/com/dev/exception/
```

The exception handler returns standardized `ErrorResponse` with:
- timestamp
- status (HTTP code)
- error (error type)
- message
- path

---

## Testing Guidelines

### Test Structure
```java
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        // Setup test data
    }

    @Test
    void testCreateBook_Success() {
        // Given
        BookRequest request = BookRequest.builder()
                .title("Test Book")
                .isbn("978-0-123456-78-9")
                .build();

        // When
        BookResponse response = bookService.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Book");
    }
}
```

- Use `@SpringBootTest` for integration tests
- Use `@Transactional` to rollback after each test
- Use `@ActiveProfiles("test")` for test configuration
- Use AssertJ for assertions (`assertThat(...)`)
- Use Given-When-Then structure with comments

### Test Database Configuration
The test profile uses H2 in-memory database:
```yaml
# application.yaml test profile
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## Environment Configuration

### Environment Variables (.env)
```env
# Database
MYSQL_DATABASE=library_db
MYSQL_USER=library_user
MYSQL_PASSWORD=library_pass

# JWT
JWT_SECRET=your-secret-key-min-256-bits-for-hs256-algorithm
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Application
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=dev
```

### Application Profiles
- **dev**: Development with MySQL, verbose logging
- **test**: H2 in-memory database, no SQL logging
- **prod**: MySQL, minimal logging, ddl-auto: validate

---

## Security

### Roles
- `ADMIN`: Full access to all resources
- `LIBRARIAN`: Manage books, borrowing, reservations
- `READER`: Borrow books, view own history

### JWT
- Access token: 24 hours default
- Refresh token: 7 days default
- Include in header: `Authorization: Bearer <token>`

---

## Common Tasks

### Adding a New Feature
1. Create model/entity in appropriate package
2. Create repository interface
3. Create DTOs (Request, Response)
4. Create service interface and implementation
5. Create controller with proper annotations
6. Add message constants if needed
7. Add Swagger documentation
8. Write tests

### Running a Single Test
```bash
mvn test -Dtest=ClassName#methodName
```

### Checking Code Coverage
```bash
mvn clean test jacoco:report
# Open target/site/jacoco/index.html in browser
```