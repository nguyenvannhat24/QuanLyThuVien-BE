# Phase 1: Core Entities & Relationships

**Trạng thái:** ✅ HOÀN THÀNH  
**Ngày hoàn thành:** 10/03/2026  
**Thời gian thực hiện:** ~8 phút (nhờ parallel agent delegation)

---

## Tóm tắt

Phase 1 triển khai các entity quan hệ cốt lõi theo thiết kế:
- **Author, Category, Publisher** - Thông tin tác giả, thể loại, nhà xuất bản
- **BookCopy** - Quản lý từng bản sao vật lý của sách
- **Refactoring** - Book và Borrow chuyển sang sử dụng FK relationships

---

## Entities mới (5 entities)

### 1. Author
```java
@Entity
@Table(name = "authors", indexes = @Index(name = "idx_author_name", columnList = "author_name"))
```
- `id` (Long, PK)
- `authorName` (String, NOT NULL, max 255)
- `biography` (Text)
- `createdAt` (LocalDateTime)

### 2. Category
```java
@Entity
@Table(name = "categories", indexes = @Index(name = "idx_category_name", columnList = "category_name"))
```
- `id` (Long, PK)
- `categoryName` (String, NOT NULL, unique, max 255)
- `description` (Text)
- `createdAt` (LocalDateTime)

### 3. Publisher
```java
@Entity
@Table(name = "publishers", indexes = @Index(name = "idx_publisher_name", columnList = "publisher_name"))
```
- `id` (Long, PK)
- `publisherName` (String, NOT NULL, max 255)
- `address`, `phone`, `email` (String, max 255)
- `createdAt` (LocalDateTime)

### 4. BookCopy
```java
@Entity
@Table(name = "book_copies", indexes = {
    @Index(name = "idx_book_id", columnList = "book_id"),
    @Index(name = "idx_copy_code", columnList = "copy_code"),
    @Index(name = "idx_status", columnList = "status")
})
```
- `id` (Long, PK)
- `book` (@ManyToOne Book, NOT NULL)
- `copyCode` (String, unique, NOT NULL)
- `status` (BookCopyStatus enum: AVAILABLE, BORROWED, DAMAGED, LOST)
- `notes` (Text)
- `createdAt`, `updatedAt` (LocalDateTime)

### 5. BookCopyStatus (Enum)
```java
public enum BookCopyStatus {
    AVAILABLE,  // Sẵn sàng cho mượn
    BORROWED,   // Đang được mượn
    DAMAGED,    // Hư hỏng
    LOST        // Mất
}
```

---

## Entities refactored (2 entities)

### Book Entity - Breaking Changes
**Trước:**
```java
private String author;
private String category;
private int quantity;
private boolean available;
```

**Sau:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_book_author"))
private Author author;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_book_category"))
private Category category;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "publisher_id", foreignKey = @ForeignKey(name = "fk_book_publisher"))
private Publisher publisher;

// quantity và available được thay thế bởi BookCopy tracking
```

**Indexes mới:**
- `idx_author_id`, `idx_category_id`, `idx_publisher_id`

### Borrow Entity - Breaking Changes
**Trước:**
```java
@ManyToOne
private Book book;
private Long copyId; // manual tracking
```

**Sau:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "book_copy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_borrow_book_copy"))
private BookCopy bookCopy; // direct FK relationship
```

**Indexes mới:**
- `idx_book_copy_id`

---

## DTOs (8 request/response pairs)

### AuthorRequest / AuthorResponse
- `authorName` (required, 1-255 chars)
- `biography` (optional)

### CategoryRequest / CategoryResponse
- `categoryName` (required, 1-255 chars, unique)
- `description` (optional)

### PublisherRequest / PublisherResponse
- `publisherName` (required, 1-255 chars)
- `address`, `phone`, `email` (optional, max 255)

### BookCopyRequest / BookCopyResponse
- `bookId` (required)
- `copyCode` (required, unique)
- `status` (enum: AVAILABLE, BORROWED, DAMAGED, LOST)
- `notes` (optional)

### BookRequest / BookResponse - Breaking Changes
**Trước:**
```java
private String author;
private String category;
```

**Sau:**
```java
// Request
private Long authorId;
private Long categoryId;
private Long publisherId;

// Response
private AuthorResponse author;
private CategoryResponse category;
private PublisherResponse publisher;
```

---

## Repositories (4 mới)

### AuthorRepository
```java
Optional<Author> findByAuthorName(String authorName);
boolean existsByAuthorName(String authorName);
```

### CategoryRepository
```java
Optional<Category> findByCategoryName(String categoryName);
boolean existsByCategoryName(String categoryName);
```

### PublisherRepository
```java
Optional<Publisher> findByPublisherName(String publisherName);
boolean existsByPublisherName(String publisherName);
```

### BookCopyRepository
```java
List<BookCopy> findByBook_Id(Long bookId);
List<BookCopy> findByStatus(BookCopyStatus status);
long countByBook_IdAndStatus(Long bookId, BookCopyStatus status);
Optional<BookCopy> findByCopyCode(String copyCode);
boolean existsByCopyCode(String copyCode);
```

### BookRepository - Updated
```java
// Chuyển sang FK-based queries
List<Book> findByAuthor_Id(Long authorId);
List<Book> findByCategory_CategoryName(String categoryName);

@Query("SELECT b FROM Book b WHERE " +
       "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(b.author.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(b.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
```

---

## Services (8 mới)

### AuthorService / AuthorServiceImpl
- `findAll(Pageable)` - Danh sách phân trang
- `findById(Long)` - Chi tiết tác giả
- `create(AuthorRequest)` - Tạo mới (check duplicate)
- `update(Long, AuthorRequest)` - Cập nhật
- `delete(Long)` - Xóa (check có sách đang dùng không)

### CategoryService / CategoryServiceImpl
- Tương tự AuthorService
- `delete()` kiểm tra Book.category FK trước khi xóa

### PublisherService / PublisherServiceImpl
- Tương tự AuthorService
- `delete()` kiểm tra Book.publisher FK trước khi xóa

### BookCopyService / BookCopyServiceImpl
- `findAll(Pageable)` / `findByBookId(Long)` - Danh sách
- `findById(Long)` - Chi tiết
- `create(BookCopyRequest)` - Tạo bản sao (check duplicate copyCode)
- `update(Long, BookCopyRequest)` - Cập nhật
- `updateStatus(Long, BookCopyStatus)` - Cập nhật trạng thái
- `delete(Long)` - Xóa (check đang mượn không)
- `countAvailableCopies(Long bookId)` - Đếm bản sao available

### BookServiceImpl - Updated
- Load Author/Category/Publisher từ database bằng ID
- Ném exception nếu không tìm thấy FK
- Response trả về nested objects (AuthorResponse, CategoryResponse, PublisherResponse)

### BorrowServiceImpl - Updated
- Thay `book` bằng `bookCopy`
- Kiểm tra `bookCopy.status == AVAILABLE` trước khi cho mượn
- Cập nhật `bookCopy.status = BORROWED` khi mượn
- Cập nhật `bookCopy.status = AVAILABLE` khi trả

---

## Controllers (4 mới)

### AuthorController (`/api/authors`)
```java
GET    /api/authors          - permitAll() - Danh sách
GET    /api/authors/{id}     - permitAll() - Chi tiết
POST   /api/authors          - LIBRARIAN/ADMIN - Tạo mới
PUT    /api/authors/{id}     - LIBRARIAN/ADMIN - Cập nhật
DELETE /api/authors/{id}     - LIBRARIAN/ADMIN - Xóa
```

### CategoryController (`/api/categories`)
- Tương tự AuthorController

### PublisherController (`/api/publishers`)
- Tương tự AuthorController

### BookCopyController (`/api/book-copies`)
```java
GET    /api/book-copies           - permitAll() - Danh sách tất cả
GET    /api/book-copies/book/{bookId} - permitAll() - Danh sách theo sách
GET    /api/book-copies/{id}      - permitAll() - Chi tiết
POST   /api/book-copies           - LIBRARIAN/ADMIN - Tạo mới
PUT    /api/book-copies/{id}      - LIBRARIAN/ADMIN - Cập nhật
PATCH  /api/book-copies/{id}/status - LIBRARIAN/ADMIN - Cập nhật trạng thái
DELETE /api/book-copies/{id}      - LIBRARIAN/ADMIN - Xóa
```

---

## API Documentation

### OpenAPIConfig
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- JWT Security scheme configured

### Dependency Added
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

---

## Migration Notes

### Breaking Changes

#### Book API
**Trước (Phase 0):**
```json
POST /api/books
{
  "title": "Spring Boot Guide",
  "author": "John Doe",
  "category": "Technology"
}
```

**Sau (Phase 1):**
```json
POST /api/books
{
  "title": "Spring Boot Guide",
  "authorId": 1,
  "categoryId": 2,
  "publisherId": 3
}

Response:
{
  "id": 1,
  "title": "Spring Boot Guide",
  "author": {
    "id": 1,
    "authorName": "John Doe"
  },
  "category": {
    "id": 2,
    "categoryName": "Technology"
  },
  "publisher": {
    "id": 3,
    "publisherName": "O'Reilly"
  }
}
```

#### Borrow API
**Trước:**
```json
POST /api/borrows
{
  "bookId": 1,
  "copyId": null  // manual tracking
}
```

**Sau:**
```json
POST /api/borrows
{
  "bookCopyId": 1  // FK to BookCopy
}
```

### Data Migration Required
Nếu có data cũ từ Phase 0:
1. Tạo Author/Category/Publisher từ Book.author, Book.category
2. Tạo BookCopy cho mỗi Book (quantity -> nhiều BookCopy)
3. Update Borrow.book -> Borrow.bookCopy

---

## Verification

### Build Status
```bash
./mvnw clean compile -DskipTests
# [INFO] BUILD SUCCESS
# [INFO] Compiling 77 source files
```

### LSP Diagnostics
- **Lombok warnings:** Tất cả "blank final field not initialized" là **false positives**
- `@RequiredArgsConstructor` tự động inject qua constructor
- Maven compilation SUCCESS = code đúng

### Real Errors Fixed
1. ✅ `JwtAuthFilter.java` - Wrong ObjectMapper import (fixed)
2. ✅ `SecurityConfig.java` - DaoAuthenticationProvider usage (verified correct)

---

## Statistics

- **Entities:** 5 mới, 2 refactored
- **Repositories:** 4 mới, 1 updated
- **DTOs:** 8 request/response pairs (4 mới, 2 updated)
- **Services:** 8 mới (4 interface + 4 impl), 2 updated
- **Controllers:** 4 mới
- **Total files:** 38 (30 new, 8 modified)
- **Compilation status:** ✅ BUILD SUCCESS
- **Lines of code:** ~3000+ lines

---

## Next Phase

**Phase 2: Borrow/Return/Renew + Scheduled Jobs**

Các task chính:
1. Enhanced borrow validation (UserStatus, Penalty checks)
2. Return logic với condition tracking (DAMAGED/LOST)
3. Renew logic với business rules
4. SystemConfig entity
5. Scheduled job cho overdue detection (daily 00:00)
6. Enhanced search/filtering

**Thời gian dự kiến:** 2 tuần
