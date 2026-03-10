# KẾ HOẠCH TRIỂN KHAI HỆ THỐNG QUẢN LÝ THƯ VIỆN

## EXECUTIVE SUMMARY

**Tình trạng hiện tại:** Codebase ban đầu với MongoDB, có Auth (JWT) + CRUD cơ bản cho User/Book/Borrow.

**Mục tiêu:** Hệ thống quản lý thư viện hoàn chỉnh với MySQL/PostgreSQL, đầy đủ nghiệp vụ theo tài liệu thiết kế.

**Phương pháp:** Migration sang RDBMS + xây dựng các module mới theo 4 giai đoạn.

---

## GAP ANALYSIS - ĐÁNH GIÁ KHOẢNG CÁCH

### ✅ ĐÃ CÓ (Cần refactor)

| Component | Status | Issues |
|-----------|--------|--------|
| User Entity | ✅ Có | ❌ Thiếu `fullName`, `status` enum (ACTIVE/INACTIVE/LOCKED), `createdAt` |
| Book Entity | ✅ Có | ❌ Thiếu `publishYear`, `description`<br>❌ `author/category` là String thay vì FK<br>❌ Không có Publisher FK<br>❌ `quantity` trong Book thay vì bảng BookCopy riêng |
| Borrow Entity | ✅ Có | ❌ Thiếu `copyId` (FK → BookCopy)<br>❌ `extendCount` nên đổi thành `renewCount` |
| Auth System | ✅ Có | ✅ JWT + BCrypt hoạt động<br>⚠️ Cần verify role-based authorization |
| Controllers | ✅ Có 6 | ✅ Cấu trúc tốt |
| DTOs | ✅ Có | ⚠️ Cần sync với entities mới |

### ❌ THIẾU HOÀN TOÀN

| Component | Priority | Impact |
|-----------|----------|--------|
| **RDBMS Migration** | 🔴 CRITICAL | Toàn bộ data model sai kiến trúc |
| **Author/Category/Publisher** | 🔴 HIGH | Relationships bị vi phạm |
| **BookCopy** | 🔴 CRITICAL | Không quản lý được bản sao vật lý |
| **Reservation** | 🟡 MEDIUM | Nghiệp vụ đặt trước thiếu |
| **Penalty** | 🟡 MEDIUM | Không xử phạt vi phạm |
| **SystemConfig** | 🟡 MEDIUM | Hardcode config |
| **Scheduled Jobs** | 🔴 HIGH | Không phát hiện quá hạn tự động |
| **Reports/Statistics** | 🟢 LOW | Feature nâng cao |

### 🔧 CẦN SỬA ĐỔI NGHIÊM TRỌNG

1. **Database Layer:** MongoDB → MySQL/PostgreSQL (breaking change)
2. **Entity Relationships:** Cấu trúc lại toàn bộ models với JPA `@ManyToOne`, `@OneToMany`
3. **Business Logic:** Thêm validation rules theo tài liệu (Reader phải ACTIVE + không có Penalty chưa thanh toán)
4. **Data Integrity:** Foreign key constraints, cascading rules

---

## IMPLEMENTATION ROADMAP

### 📋 OVERVIEW

```
Phase 0 (1 week):  Database Migration & Foundation Refactor
Phase 1 (2 weeks): Core Entities & Relationships + Enhanced Auth
Phase 2 (2 weeks): Borrow/Return/Renew + Scheduled Jobs
Phase 3 (2 weeks): Reservation + Penalty + Notifications
Phase 4 (1 week):  Reports + Statistics + Admin Features
```

**Total Estimate:** 8 weeks (có thể điều chỉnh dựa trên team size)

---

## PHASE 0: DATABASE MIGRATION & FOUNDATION REFACTOR

**Duration:** 1 week  
**Goal:** Chuyển đổi sang RDBMS, thiết lập foundation chất lượng

### Tasks

#### 0.1 Setup Database Infrastructure
- [ ] Cài đặt MySQL/PostgreSQL local (Docker compose)
- [ ] Update `pom.xml`:
  ```xml
  <!-- Remove -->
  <dependency spring-boot-starter-data-mongodb />
  
  <!-- Add -->
  <dependency spring-boot-starter-data-jpa />
  <dependency mysql-connector-java /> <!-- hoặc postgresql -->
  ```
- [ ] Create `application.yml` với profiles (dev/test/prod)
- [ ] Setup connection pooling (HikariCP - mặc định Spring Boot)
- [ ] Tạo database schema: `library_db`

#### 0.2 Update Existing Entities cho JPA
- [ ] **User:**
  - Đổi `@Document` → `@Entity`
  - Thêm `@Table(name = "users")`
  - Thêm fields: `fullName`, `status` enum, `createdAt`
  - Đổi role: `USER` → `READER` (hoặc giữ USER nhưng document rõ)
  - Add JPA annotations: `@Column`, `@Enumerated`
  
- [ ] **Book:** (Tạm refactor, sẽ có BookCopy sau)
  - Đổi `@Document` → `@Entity`
  - Thêm: `publishYear`, `description`
  - GIỮ LẠI `quantity` tạm (Phase 1 sẽ tách ra BookCopy)
  - Chuẩn bị FK placeholders cho Author/Category/Publisher
  
- [ ] **Borrow:**
  - Đổi `@Document` → `@Entity`
  - Đổi `extendCount` → `renewCount`
  - Thêm `copyId` (Long) - temporary, Phase 1 sẽ thành FK

#### 0.3 Update Repositories
- [ ] Đổi từ `MongoRepository` → `JpaRepository`
- [ ] Giữ nguyên method signatures nếu tương thích
- [ ] Test các queries cơ bản

#### 0.4 Data Migration (Nếu có data production)
- [ ] Export MongoDB data (JSON)
- [ ] Script chuyển đổi ID format (MongoDB ObjectId → Long)
- [ ] Import vào MySQL/PostgreSQL
- [ ] Verify data integrity

#### 0.5 Testing & Validation
- [ ] All existing endpoints phải hoạt động như cũ
- [ ] Integration tests pass
- [ ] Smoke test trên local

**Deliverables:**
- ✅ Application chạy trên MySQL/PostgreSQL
- ✅ Existing features (auth, book CRUD, borrow) hoạt động không đổi
- ✅ Dockerfile/docker-compose.yml updated

---

## PHASE 1: CORE ENTITIES & RELATIONSHIPS

**Duration:** 2 weeks  
**Goal:** Xây dựng data model đầy đủ, relationships đúng thiết kế

### Tasks

#### 1.1 Create Reference Entities
- [ ] **Author Entity**
  ```java
  @Entity
  class Author {
    @Id @GeneratedValue Long authorId;
    String name;
    String biography;
    @OneToMany(mappedBy="author") Set<Book> books;
  }
  ```
- [ ] **Category Entity** (tương tự Author)
- [ ] **Publisher Entity** (tương tự Author)
- [ ] Tạo Repositories, Services, Controllers cho CRUD
- [ ] DTOs cho từng entity
- [ ] Validation: name unique, not blank

#### 1.2 Refactor Book Entity với Relationships
- [ ] Thêm FKs:
  ```java
  @ManyToOne @JoinColumn(name="author_id") Author author;
  @ManyToOne @JoinColumn(name="category_id") Category category;
  @ManyToOne @JoinColumn(name="publisher_id") Publisher publisher;
  ```
- [ ] Remove String `author`, `category` fields cũ
- [ ] Update BookDTO với nested AuthorDTO, CategoryDTO, PublisherDTO
- [ ] Migration script cho data cũ (map String → FK)
- [ ] Update BookController với filtering by author/category/publisher

#### 1.3 Create BookCopy Entity (CRITICAL)
- [ ] **BookCopy Entity:**
  ```java
  @Entity
  class BookCopy {
    @Id @GeneratedValue Long copyId;
    @ManyToOne Book book;
    String copyCode; // unique barcode
    @Enumerated(EnumType.STRING) CopyStatus status; // AVAILABLE, BORROWED, DAMAGED, LOST
    LocalDate acquiredDate;
    @OneToOne(mappedBy="bookCopy") BorrowRecord currentBorrow;
  }
  ```
- [ ] Generate `copyCode` tự động (format: `BOOK{bookId}-COPY{number}`)
- [ ] Migration: từ `Book.quantity` → tạo N BookCopy records
- [ ] BookCopyRepository với query:
  - `findByBookAndStatus(Book, CopyStatus)`
  - `countByBookAndStatus(Book, CopyStatus)`

#### 1.4 Refactor Borrow → BorrowRecord
- [ ] Đổi tên `Borrow` → `BorrowRecord` (match thiết kế)
- [ ] Update FK:
  ```java
  @ManyToOne User reader; // FK userId
  @ManyToOne BookCopy bookCopy; // FK copyId
  ```
- [ ] Remove `bookId` field cũ (thay bằng `bookCopy.book`)
- [ ] Business logic: check `bookCopy.status == AVAILABLE` trước khi mượn
- [ ] Khi mượn: set `bookCopy.status = BORROWED`
- [ ] Khi trả: set `bookCopy.status = AVAILABLE`, `returnDate = now()`

#### 1.5 Enhanced Authorization
- [ ] `@PreAuthorize` annotations cho tất cả endpoints
- [ ] Role matrix:
  | Endpoint | READER | LIBRARIAN | ADMIN |
  |----------|--------|-----------|-------|
  | GET /api/books | ✅ | ✅ | ✅ |
  | POST /api/books | ❌ | ✅ | ✅ |
  | POST /api/borrows | ✅ | ✅ | ✅ |
  | GET /api/admin/** | ❌ | ❌ | ✅ |
- [ ] Custom AccessDeniedException handler

#### 1.6 API Documentation
- [ ] Tích hợp Springdoc OpenAPI (Swagger UI)
- [ ] Annotate tất cả endpoints với `@Operation`, `@ApiResponse`
- [ ] Generate API docs tại `/swagger-ui.html`

**Deliverables:**
- ✅ Author/Category/Publisher CRUD hoạt động
- ✅ Book có relationships đầy đủ
- ✅ BookCopy quản lý bản sao vật lý
- ✅ BorrowRecord liên kết với BookCopy (không phải Book)
- ✅ Authorization matrix đúng
- ✅ Swagger docs

---

## PHASE 2: BORROW/RETURN/RENEW + SCHEDULED JOBS

**Duration:** 2 weeks  
**Goal:** Hoàn thiện quy trình mượn/trả, tự động hóa phát hiện quá hạn

### Tasks

#### 2.1 Borrow Business Logic Enhancement
- [ ] **Pre-borrow Validation:**
  ```java
  boolean canBorrow(User reader) {
    return reader.status == ACTIVE 
      && penaltyRepository.countByReaderAndStatus(reader, UNPAID) == 0;
  }
  ```
- [ ] Check available BookCopy: `status == AVAILABLE`
- [ ] Tính `dueDate = borrowDate + SystemConfig.default_borrow_days`
- [ ] Transaction safety: `@Transactional` cho borrow operation

#### 2.2 Return Logic
- [ ] Endpoint: `POST /api/borrows/{id}/return`
- [ ] Validation: chỉ READER/LIBRARIAN của borrow đó hoặc ADMIN
- [ ] Set `returnDate = now()`
- [ ] Kiểm tra overdue → tạo Penalty (Phase 3 sẽ làm đầy đủ)
- [ ] Update `bookCopy.status = AVAILABLE`
- [ ] Check Reservation queue → notify nếu có (Phase 3)

#### 2.3 Renew (Gia hạn) Logic
- [ ] Endpoint: `POST /api/borrows/{id}/renew`
- [ ] Validation:
  - `renewCount < SystemConfig.max_renew_count`
  - `status == BORROWING` (chưa trả)
  - Không có Reservation WAITING cho `bookCopy.book`
  - Chưa quá hạn (hoặc policy cho phép)
- [ ] Tăng `renewCount++`
- [ ] Extend `dueDate += default_borrow_days`

#### 2.4 SystemConfig Entity
- [ ] **SystemConfig:**
  ```java
  @Entity
  class SystemConfig {
    @Id String configKey; // PK
    String configValue;
    String description;
  }
  ```
- [ ] Seed data:
  ```
  default_borrow_days = 14
  max_renew_count = 2
  fine_per_day = 5000
  reservation_hold_days = 3
  max_borrow_per_reader = 5
  ```
- [ ] Service: `getConfig(key)`, `updateConfig(key, value)`
- [ ] Admin endpoint: `GET/PUT /api/admin/config`

#### 2.5 Scheduled Job: Overdue Detection
- [ ] Enable scheduling: `@EnableScheduling`
- [ ] Job class: `OverdueDetectionJob`
- [ ] Cron: hàng ngày 00:00
  ```java
  @Scheduled(cron = "0 0 0 * * ?")
  public void detectOverdue() {
    List<BorrowRecord> overdue = borrowRepository
      .findByStatusAndDueDateBefore(BORROWING, LocalDate.now());
    overdue.forEach(br -> br.setStatus(OVERDUE));
    borrowRepository.saveAll(overdue);
  }
  ```
- [ ] Logging: số lượng records detected

#### 2.6 Search & Filtering Enhancements
- [ ] Book search với filters:
  - Title/ISBN (like)
  - Author/Category/Publisher (FK)
  - Status (có copy AVAILABLE hay không)
  - Pagination với `Pageable`
- [ ] BorrowRecord search:
  - By reader
  - By status
  - By date range
  - Pagination

**Deliverables:**
- ✅ Mượn/trả/gia hạn hoạt động với đầy đủ validation
- ✅ SystemConfig quản lý config động
- ✅ Scheduled job phát hiện quá hạn tự động
- ✅ Search/filter nâng cao

---

## PHASE 3: RESERVATION + PENALTY + NOTIFICATIONS

**Duration:** 2 weeks  
**Goal:** Đặt trước sách, xử phạt vi phạm, thông báo người dùng

### Tasks

#### 3.1 Reservation Entity & Logic
- [ ] **Reservation Entity:**
  ```java
  @Entity
  class Reservation {
    @Id @GeneratedValue Long reservationId;
    @ManyToOne User reader;
    @ManyToOne Book book; // đặt trước SÁCH, không phải copy
    LocalDate reserveDate;
    LocalDate notifyDate;
    LocalDate expireDate;
    @Enumerated ReservationStatus status; // WAITING, NOTIFIED, FULFILLED, CANCELLED, EXPIRED
    Integer queuePosition;
  }
  ```
- [ ] **Business Rules:**
  - Chỉ được đặt khi **TẤT CẢ** BookCopy đều `status == BORROWED`
  - Queue FIFO: `queuePosition` tự động tăng
  - `queuePosition` recalculate khi có cancel/fulfill
  
- [ ] **API:**
  - `POST /api/reservations` - Đặt trước
  - `GET /api/reservations/my` - Xem hàng đợi của mình
  - `DELETE /api/reservations/{id}` - Hủy đặt

#### 3.2 Reservation Queue Management
- [ ] Khi BookCopy trả về (return):
  - Check hàng đợi: `findByBookAndStatusOrderByQueuePosition(book, WAITING)`
  - Lấy top 1 reservation
  - Update `status = NOTIFIED`, `notifyDate = now()`, `expireDate = now() + reservation_hold_days`
  - Gửi thông báo (email/in-app)
  
- [ ] Scheduled job: Expire reservations
  ```java
  @Scheduled(cron = "0 0 1 * * ?") // 01:00 daily
  findByStatusAndExpireDateBefore(NOTIFIED, now())
    .forEach(r -> r.setStatus(EXPIRED));
  ```

#### 3.3 Fulfill Reservation
- [ ] Khi READER mượn sách đang có reservation NOTIFIED:
  - Check: `reader == reservation.reader`
  - Set `reservation.status = FULFILLED`
  - Proceed với borrow thông thường

#### 3.4 Penalty Entity & Logic
- [ ] **Penalty Entity:**
  ```java
  @Entity
  class Penalty {
    @Id @GeneratedValue Long penaltyId;
    @ManyToOne BorrowRecord borrowRecord;
    @ManyToOne User reader;
    @Enumerated PenaltyType type; // OVERDUE, DAMAGED, LOST
    BigDecimal amount;
    @Enumerated PenaltyStatus status; // UNPAID, PAID, WAIVED
    LocalDate createdDate;
    LocalDate paidDate;
    String notes;
  }
  ```
  
- [ ] **Penalty Creation:**
  - **OVERDUE:** Scheduled job hoặc return time
    ```java
    long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
    if (daysOverdue > 0) {
      BigDecimal fine = SystemConfig.fine_per_day * daysOverdue;
      createPenalty(borrow, reader, OVERDUE, fine);
    }
    ```
  - **DAMAGED/LOST:** Librarian manual action
    - Endpoint: `POST /api/penalties` (LIBRARIAN only)
    - Input: borrowId, type, amount, notes
  
- [ ] **Penalty Enforcement:**
  - Validation trong `canBorrow()`: check unpaid penalties
  - Reader không mượn được nếu có UNPAID

- [ ] **Payment:**
  - `POST /api/penalties/{id}/pay` (READER/LIBRARIAN/ADMIN)
  - Set `status = PAID`, `paidDate = now()`
  - Simple implementation: không tích hợp payment gateway (chỉ mark paid)

#### 3.5 Notification System (Simple)
- [ ] **Notification Entity:**
  ```java
  @Entity
  class Notification {
    @Id @GeneratedValue Long notificationId;
    @ManyToOne User user;
    String title;
    String message;
    @Enumerated NotificationType type; // RESERVATION_READY, OVERDUE_REMINDER, PENALTY_CREATED
    Boolean isRead;
    LocalDateTime createdAt;
  }
  ```
  
- [ ] **Trigger points:**
  - Reservation ready: khi BookCopy available
  - Overdue reminder: 1 ngày trước `dueDate` (scheduled job)
  - Penalty created: khi tạo penalty
  
- [ ] **API:**
  - `GET /api/notifications/my` - Lấy thông báo của user
  - `PUT /api/notifications/{id}/read` - Đánh dấu đã đọc

- [ ] **Email (Optional):**
  - Tích hợp Spring Mail
  - Async sending với `@Async`
  - Template engine (Thymeleaf) cho email HTML

**Deliverables:**
- ✅ Reservation FIFO queue hoạt động
- ✅ Penalty tự động (overdue) và manual (damaged/lost)
- ✅ Notification in-app
- ✅ Validation: Reader không mượn nếu có penalty chưa thanh toán

---

## PHASE 4: REPORTS + STATISTICS + ADMIN FEATURES

**Duration:** 1 week  
**Goal:** Báo cáo, thống kê, quản trị nâng cao

### Tasks

#### 4.1 Statistics Service
- [ ] **Dashboard Metrics:**
  - Tổng sách/độc giả/mượn hiện tại
  - Top 10 sách được mượn nhiều nhất
  - Borrowing trends (theo tháng/năm)
  - Overdue rate
  - Penalty revenue
  
- [ ] **Queries với JPA:**
  ```java
  @Query("SELECT b.book.title, COUNT(b) FROM BorrowRecord b GROUP BY b.book ORDER BY COUNT(b) DESC")
  List<Object[]> findTopBorrowedBooks(Pageable pageable);
  ```

#### 4.2 Reports API
- [ ] **Endpoints (LIBRARIAN/ADMIN only):**
  - `GET /api/reports/dashboard` - Dashboard metrics
  - `GET /api/reports/borrow-history?startDate&endDate` - Lịch sử mượn
  - `GET /api/reports/overdue` - Danh sách quá hạn hiện tại
  - `GET /api/reports/penalties?status` - Báo cáo phạt
  - `GET /api/reports/popular-books?limit` - Sách phổ biến
  
- [ ] **Export to CSV/Excel (Optional):**
  - Apache POI library
  - Endpoint: `GET /api/reports/{type}/export?format=csv`

#### 4.3 Admin Features
- [ ] **User Management:**
  - `PUT /api/admin/users/{id}/lock` - Khóa tài khoản
  - `PUT /api/admin/users/{id}/unlock` - Mở khóa
  - `PUT /api/admin/users/{id}/role` - Đổi role
  
- [ ] **Bulk Operations:**
  - `POST /api/admin/books/bulk-import` - Import CSV
  - `POST /api/admin/book-copies/generate` - Tạo nhiều copy cùng lúc
  
- [ ] **Audit Log (Optional but recommended):**
  ```java
  @Entity
  class AuditLog {
    @Id @GeneratedValue Long id;
    @ManyToOne User actor;
    String action; // "LOCK_USER", "CREATE_BOOK", etc.
    String entityType;
    Long entityId;
    String details; // JSON
    LocalDateTime timestamp;
  }
  ```
  - AOP `@Around` advice để log các admin actions

#### 4.4 Advanced Search
- [ ] **Specification Pattern cho dynamic queries:**
  ```java
  interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {}
  ```
- [ ] **Search Builder:**
  - Multi-field search: title OR author OR isbn
  - Filters: category, publisher, availableCopies > 0
  - Sorting: title, publishYear
  - Pagination

#### 4.5 System Health & Monitoring
- [ ] Spring Boot Actuator
  - `/actuator/health` - Health check
  - `/actuator/metrics` - Metrics
  - Secure với Spring Security
  
- [ ] **Database Backup Script (Admin endpoint):**
  - `POST /api/admin/backup` - Trigger mysqldump
  - Store backup file với timestamp

**Deliverables:**
- ✅ Dashboard với metrics quan trọng
- ✅ Reports API đầy đủ
- ✅ Admin features (lock/unlock user, bulk operations)
- ✅ Audit log
- ✅ Advanced search
- ✅ Monitoring setup

---

## CROSS-CUTTING CONCERNS

### Testing Strategy

#### Unit Tests
- [ ] Service layer: JUnit 5 + Mockito
- [ ] Coverage target: >80% cho services
- [ ] Test cases: success paths + edge cases + exceptions

#### Integration Tests
- [ ] `@SpringBootTest` với H2 in-memory database
- [ ] Test repositories với real queries
- [ ] Test API endpoints với `@WebMvcTest`

#### E2E Tests (Optional)
- [ ] Postman collections
- [ ] Sample test scenarios:
  1. User register → login → borrow book → return
  2. Reservation flow: đặt trước → nhận thông báo → mượn
  3. Overdue → penalty → payment

### Security Hardening
- [ ] SQL Injection protection: JPA Parameterized queries (mặc định safe)
- [ ] XSS protection: Response escaping (Spring MVC mặc định)
- [ ] CSRF: Enable cho non-API endpoints (nếu có web views)
- [ ] Rate limiting: Spring Rate Limit hoặc Bucket4j
- [ ] Input validation: `@Valid` trên tất cả DTOs

### Performance Optimization
- [ ] Database indexing:
  ```sql
  CREATE INDEX idx_book_isbn ON books(isbn);
  CREATE INDEX idx_borrow_reader_status ON borrow_records(reader_id, status);
  CREATE INDEX idx_reservation_book_status ON reservations(book_id, status);
  ```
- [ ] Lazy loading cho relationships (JPA mặc định)
- [ ] Query optimization: avoid N+1 với `@EntityGraph` hoặc JOIN FETCH
- [ ] Caching: Spring Cache cho `SystemConfig`, popular books

### Documentation
- [ ] README.md: Setup instructions, architecture overview
- [ ] CONTRIBUTING.md: Coding standards, PR process
- [ ] API Documentation: Swagger + Postman collection
- [ ] Database ERD: dbdiagram.io hoặc draw.io
- [ ] Deployment guide: Docker compose, env variables

---

## DEPLOYMENT CHECKLIST

### Local Development
- [ ] Docker Compose với MySQL + phpMyAdmin
- [ ] `.env` file với secrets (gitignored)
- [ ] Hot reload với Spring DevTools

### Staging/Production
- [ ] Environment-specific `application-{profile}.yml`
- [ ] Database migration strategy (Flyway hoặc Liquibase)
- [ ] Docker image build: multi-stage Dockerfile
- [ ] CI/CD pipeline: GitHub Actions hoặc GitLab CI
- [ ] Secrets management: Vault hoặc cloud secrets manager
- [ ] Monitoring: Prometheus + Grafana
- [ ] Logging: ELK stack hoặc cloud logging
- [ ] SSL/TLS certificates

---

## RISK ASSESSMENT

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data loss trong migration MongoDB → MySQL | 🔴 HIGH | Backup đầy đủ, dry-run migration, rollback plan |
| Performance bottleneck với relationships phức tạp | 🟡 MEDIUM | Indexing strategy, query optimization, caching |
| Reservation queue race condition | 🟡 MEDIUM | Database-level locking, `@Transactional(isolation=SERIALIZABLE)` |
| Scheduled job failure không được phát hiện | 🟡 MEDIUM | Health checks cho jobs, alerting |
| Breaking changes từ Phase 0 ảnh hưởng frontend | 🔴 HIGH | API versioning (`/api/v1/`), deprecation notices |

---

## SUCCESS CRITERIA

- ✅ 100% functional requirements từ tài liệu thiết kế implemented
- ✅ Zero critical security vulnerabilities (OWASP Top 10)
- ✅ API response time < 200ms (p95)
- ✅ Database migration thành công không mất data
- ✅ Test coverage > 80%
- ✅ All scheduled jobs chạy đúng lịch
- ✅ Documentation đầy đủ cho developer mới

---

## APPENDIX

### Useful Commands

```bash
# Database migration dry-run
mysqldump library_db > backup_$(date +%Y%m%d).sql

# Run tests
./mvnw test

# Build Docker image
docker build -t library-api:latest .

# Run with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f api
```

### Reference Links
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Swagger OpenAPI: https://springdoc.org/
- Docker Compose: https://docs.docker.com/compose/

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-10  
**Author:** AI Assistant (dựa trên tai-lieu-thiet-ke.md)
