# 📚 Tài Liệu Thiết Kế Phần Mềm — Hệ Thống Quản Lý Thư Viện Sách

> **Mục đích tài liệu:** Cung cấp đầy đủ thông tin để AI Coding Agent hiểu đúng bài toán, phạm vi hệ thống, kiến trúc kỹ thuật và danh sách chức năng cần triển khai — tránh suy diễn sai yêu cầu hoặc bỏ sót nghiệp vụ.

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1 Bài toán cần giải quyết

Hệ thống quản lý thư viện sách thay thế các quy trình thủ công (sổ sách, công cụ rời rạc) bằng một nền tảng phần mềm tập trung, bao gồm:

- Quản lý tài nguyên thư viện (sách, bản sao vật lý)
- Quản lý người dùng (Người đọc, Thủ thư, Quản trị viên)
- Quản lý nghiệp vụ mượn – trả – gia hạn – đặt trước
- Xử lý vi phạm (trễ hạn, mất/hư sách)
- Thống kê, báo cáo và quản trị hệ thống

### 1.2 Phạm vi hệ thống (In-Scope)

| Nhóm chức năng | Mô tả tóm tắt |
|---|---|
| Xác thực & phân quyền | Đăng ký, đăng nhập, JWT, phân quyền 3 role |
| Quản lý người dùng | CRUD Reader, quản lý trạng thái tài khoản |
| Quản lý sách | CRUD Book, Author, Category, Publisher |
| Quản lý bản sao sách | Theo dõi số lượng và trạng thái vật lý từng bản |
| Mượn – Trả – Gia hạn | Toàn bộ vòng đời một giao dịch mượn |
| Đặt trước sách | Queue đặt chỗ, thông báo khi sách khả dụng |
| Xử phạt vi phạm | Tính phạt trễ hạn, mất/hư sách |
| Tra cứu sách | Tìm kiếm cơ bản và nâng cao |
| Thống kê & báo cáo | Lượt mượn, sách hot, tình trạng kho |
| Quản trị hệ thống | Cấu hình, sao lưu, log, quản lý tài khoản |

### 1.3 Ngoài phạm vi (Out-of-Scope)

- Quản lý ebook / tài liệu số
- Thanh toán trực tuyến
- Tích hợp RFID / QR Code
- Ứng dụng mobile native

---

## 2. KIẾN TRÚC KỸ THUẬT

### 2.1 Stack công nghệ

| Tầng | Công nghệ |
|---|---|
| **Frontend** | Angular (component-based, Angular Router, RxJS/Observable) |
| **Backend** | Spring Boot (REST API, Spring Security, Spring Data JPA) |
| **Database** | Quan hệ (MySQL / PostgreSQL) |
| **Xác thực** | JWT (JSON Web Token) |
| **ORM** | Hibernate / Spring Data JPA |
| **Bảo mật mật khẩu** | BCrypt |
| **Quản lý mã nguồn** | Git |

### 2.2 Kiến trúc phân tầng Backend

```
Client (Angular)
    │  HTTP/JSON
    ▼
Controller Layer       ← Tiếp nhận request REST, validate input
    │
    ▼
Service Layer          ← Xử lý toàn bộ business logic
    │
    ▼
Repository Layer       ← Truy vấn CSDL qua Spring Data JPA
    │
    ▼
Database (MySQL/PostgreSQL)
```

### 2.3 Mô hình MVC

- **Model:** Các entity: `User`, `Book`, `BookCopy`, `BorrowRecord`, `Reservation`, `Penalty`, `Category`, `Author`, `Publisher`
- **View:** Angular components, phân theo module role
- **Controller:** Spring REST Controllers, ánh xạ endpoints

### 2.4 Cơ chế bảo mật

- Mật khẩu hash bằng BCrypt
- Xác thực stateless bằng JWT
- Spring Security filter chain kiểm tra token mỗi request
- Phân quyền theo role: `READER`, `LIBRARIAN`, `ADMIN`
- Bảo vệ chống SQL Injection (parameterized query qua JPA), XSS, CSRF

---

## 3. ACTORS VÀ PHÂN QUYỀN

### 3.1 Các Actor

| Actor | Loại | Mô tả |
|---|---|---|
| **Reader** (Người đọc) | Primary | Sinh viên, giảng viên, người dùng mượn sách |
| **Librarian** (Thủ thư) | Primary | Nhân viên quản lý nghiệp vụ hằng ngày |
| **Admin** (Quản trị viên) | Primary | Quản lý hệ thống toàn diện |
| **Notification System** | Secondary | Hệ thống email/thông báo tự động |

### 3.2 Ma trận phân quyền

| Chức năng | Reader | Librarian | Admin |
|---|:---:|:---:|:---:|
| Đăng ký / Đăng nhập | ✅ | ✅ | ✅ |
| Tra cứu sách | ✅ | ✅ | ✅ |
| Mượn / Trả / Gia hạn | ✅ (self) | ✅ (all) | ✅ |
| Đặt trước sách | ✅ | ✅ | ✅ |
| Xem lịch sử cá nhân | ✅ (self) | ✅ (all) | ✅ |
| CRUD sách & bản sao | ❌ | ✅ | ✅ |
| CRUD Author/Category/Publisher | ❌ | ✅ | ✅ |
| Ghi nhận vi phạm | ❌ | ✅ | ✅ |
| Quản lý tài khoản người dùng | ❌ | ❌ | ✅ |
| Cấu hình hệ thống | ❌ | ❌ | ✅ |
| Xem log hệ thống | ❌ | ❌ | ✅ |
| Sao lưu / Phục hồi dữ liệu | ❌ | ❌ | ✅ |
| Xem & xuất báo cáo | ❌ | ✅ | ✅ |

---

## 4. DANH SÁCH CHỨC NĂNG CHI TIẾT

### 4.1 Xác thực và tài khoản

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-01 | Đăng ký tài khoản | Reader | Reader tạo tài khoản mới, chờ Admin duyệt/kích hoạt |
| F-02 | Đăng nhập | All | Xác thực bằng username/password, nhận JWT |
| F-03 | Đăng xuất | All | Hủy phiên làm việc, invalidate token phía client |
| F-04 | Đổi mật khẩu | All | Người dùng tự đổi mật khẩu sau khi xác thực |

> ⚠️ **Lưu ý:** Tài khoản mới cần Admin **duyệt/kích hoạt** trước khi sử dụng — không tự động active.

---

### 4.2 Quản lý người dùng (Admin)

| ID | Tên chức năng | Mô tả |
|---|---|---|
| F-10 | Xem danh sách người dùng | Lọc theo role, trạng thái, tên |
| F-11 | Tạo tài khoản người dùng | Admin tạo tài khoản Librarian/Reader thủ công |
| F-12 | Cập nhật thông tin người dùng | Sửa tên, email, role |
| F-13 | Khóa / Mở khóa tài khoản | Vô hiệu hóa tài khoản vi phạm hoặc kích hoạt lại |
| F-14 | Xóa tài khoản | Xóa mềm (soft delete) |

---

### 4.3 Quản lý sách (Librarian/Admin)

| ID | Tên chức năng | Mô tả |
|---|---|---|
| F-20 | Thêm đầu sách mới | Nhập: tên, ISBN, tác giả, thể loại, NXB, năm xuất bản, mô tả |
| F-21 | Cập nhật thông tin sách | Sửa thông tin metadata của đầu sách |
| F-22 | Xóa đầu sách | Xóa khi không còn bản sao nào đang được mượn |
| F-23 | Quản lý Thể loại (Category) | CRUD Category |
| F-24 | Quản lý Tác giả (Author) | CRUD Author |
| F-25 | Quản lý Nhà xuất bản (Publisher) | CRUD Publisher |

---

### 4.4 Quản lý bản sao sách — BookCopy (Librarian/Admin)

| ID | Tên chức năng | Mô tả |
|---|---|---|
| F-30 | Thêm bản sao | Thêm 1 hoặc nhiều bản sao vật lý cho một đầu sách |
| F-31 | Cập nhật trạng thái bản sao | Trạng thái: `AVAILABLE` / `BORROWED` / `DAMAGED` / `LOST` |
| F-32 | Xóa bản sao | Xóa bản sao bị hư hỏng/thanh lý |

> ⚠️ **Lưu ý thiết kế quan trọng:**
> - `Book` = thông tin logic (metadata) của một đầu sách
> - `BookCopy` = bản sao vật lý thực tế trong kho (mỗi bản có ID riêng)
> - Khi mượn sách → hệ thống gắn vào 1 `BookCopy` cụ thể, không phải `Book`

---

### 4.5 Mượn sách

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-40 | Gửi yêu cầu mượn sách | Reader | Chọn đầu sách, gửi yêu cầu |
| F-41 | Xác nhận cho mượn | Librarian | Gắn `BookCopy` cụ thể, xác nhận, ghi `BorrowRecord` |
| F-42 | Xem lịch sử mượn | Reader (self) / Librarian (all) | Danh sách các lượt mượn với trạng thái |

**Luồng mượn sách:**
```
Reader gửi yêu cầu
    → Hệ thống kiểm tra tài khoản không bị khóa & không có vi phạm chưa xử lý
    → Librarian xác nhận
    → Hệ thống chọn BookCopy AVAILABLE, đổi trạng thái → BORROWED
    → Ghi BorrowRecord (readerId, bookCopyId, borrowDate, dueDate, status=BORROWING)
    → Gửi thông báo xác nhận cho Reader
```

---

### 4.6 Trả sách

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-50 | Xử lý trả sách | Librarian | Kiểm tra tình trạng sách, cập nhật hệ thống |
| F-51 | Tính phạt trễ hạn | Hệ thống tự động | Tính khi ngày trả > dueDate |

**Luồng trả sách:**
```
Reader mang sách đến quầy
    → Librarian tìm BorrowRecord
    → Kiểm tra tình trạng sách (bình thường / hư hỏng / mất)
    → Nếu trễ hạn: tạo Penalty (type=OVERDUE, amount=tính theo cấu hình)
    → Nếu hư/mất: tạo Penalty (type=DAMAGED/LOST)
    → Cập nhật BorrowRecord.status = RETURNED, returnDate = ngày hôm nay
    → Cập nhật BookCopy.status = AVAILABLE (hoặc DAMAGED/LOST)
    → Kích hoạt Reservation tiếp theo nếu có (xem F-60)
```

---

### 4.7 Gia hạn mượn

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-55 | Yêu cầu gia hạn | Reader | Gửi yêu cầu gia hạn trước ngày hết hạn |
| F-56 | Xác nhận gia hạn | Librarian | Duyệt hoặc từ chối |

**Điều kiện gia hạn:**
- Không có Reservation đang chờ cho đầu sách này
- Tài khoản Reader không bị khóa
- Chưa gia hạn quá số lần tối đa (theo cấu hình hệ thống)

---

### 4.8 Đặt trước sách (Reservation)

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-60 | Đặt trước sách | Reader | Khi tất cả bản sao đang được mượn |
| F-61 | Hủy đặt trước | Reader | Reader tự hủy trước khi sách khả dụng |
| F-62 | Thông báo sách khả dụng | Notification System | Gửi email/thông báo khi sách được trả |

**Luồng đặt trước:**
```
Reader yêu cầu đặt trước Book (tất cả BookCopy đều BORROWED)
    → Hệ thống tạo Reservation (readerId, bookId, reserveDate, status=WAITING, queuePosition)
    → Khi có BookCopy trả về → hệ thống tìm Reservation tiếp theo (FIFO)
    → Gửi thông báo cho Reader đó → Reader có N ngày để đến mượn (theo cấu hình)
    → Nếu Reader không đến → hủy Reservation, chuyển sang người tiếp theo
```

---

### 4.9 Xử phạt vi phạm

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-70 | Ghi nhận phạt trễ hạn | Hệ thống / Librarian | Tự động khi trả trễ |
| F-71 | Ghi nhận phạt mất/hư sách | Librarian | Nhập thủ công khi kiểm tra tình trạng |
| F-72 | Xem danh sách vi phạm | Reader (self) / Librarian / Admin | |
| F-73 | Đánh dấu đã thanh toán phạt | Librarian | Sau khi Reader nộp phạt |

**Loại vi phạm:**
- `OVERDUE`: Trả trễ → tính theo số ngày × đơn giá phạt/ngày (từ config)
- `DAMAGED`: Sách hư hỏng → mức phạt cố định hoặc % giá sách (từ config)
- `LOST`: Mất sách → mức phạt bằng giá trị sách (từ config)

> ⚠️ Reader có vi phạm chưa thanh toán → **không được mượn thêm sách mới**

---

### 4.10 Tra cứu và tìm kiếm sách

| ID | Tên chức năng | Actor | Mô tả |
|---|---|---|---|
| F-80 | Tìm kiếm cơ bản | All | Tìm theo từ khóa: tên sách, tác giả, thể loại |
| F-81 | Tìm kiếm nâng cao | All | Lọc theo: NXB, năm xuất bản, tình trạng còn/đã mượn |
| F-82 | Xem chi tiết sách | All | Hiển thị đầy đủ metadata + tình trạng từng bản sao |

---

### 4.11 Thống kê và báo cáo (Librarian/Admin)

| ID | Tên chức năng | Mô tả |
|---|---|---|
| F-90 | Thống kê lượt mượn | Theo ngày/tháng/năm |
| F-91 | Sách được mượn nhiều nhất | Top N đầu sách theo số lượt mượn |
| F-92 | Tình trạng kho sách | Số bản sao theo từng trạng thái |
| F-93 | Thống kê vi phạm | Số lượng vi phạm theo loại, theo thời gian |
| F-94 | Báo cáo Reader hoạt động | Reader mượn nhiều nhất, Reader vi phạm nhiều |
| F-95 | Xuất báo cáo | Export ra file (PDF/Excel) |

---

### 4.12 Quản trị hệ thống (Admin)

| ID | Tên chức năng | Mô tả |
|---|---|---|
| F-100 | Cấu hình hệ thống | Thiết lập: thời gian mượn mặc định, số lần gia hạn tối đa, mức phạt/ngày, thời gian giữ Reservation |
| F-101 | Xem log hệ thống | Tra cứu log theo thời gian, loại sự kiện, user |
| F-102 | Sao lưu dữ liệu | Trigger backup thủ công hoặc xem lịch sử backup tự động |
| F-103 | Phục hồi dữ liệu | Restore từ bản backup đã chọn |

---

## 5. MÔ HÌNH DỮ LIỆU

### 5.1 Các Entity chính

```
User
├── userId (PK)
├── username
├── passwordHash
├── email
├── fullName
├── role: ENUM(READER, LIBRARIAN, ADMIN)
├── status: ENUM(ACTIVE, INACTIVE, LOCKED)
└── createdAt

Book
├── bookId (PK)
├── title
├── isbn
├── description
├── publishYear
├── authorId (FK → Author)
├── categoryId (FK → Category)
├── publisherId (FK → Publisher)
└── createdAt

BookCopy
├── copyId (PK)
├── bookId (FK → Book)
├── copyCode         ← mã vật lý (barcode / số thứ tự)
├── status: ENUM(AVAILABLE, BORROWED, DAMAGED, LOST)
└── createdAt

BorrowRecord
├── borrowId (PK)
├── readerId (FK → User)
├── copyId (FK → BookCopy)
├── borrowDate
├── dueDate
├── returnDate (nullable)
├── status: ENUM(BORROWING, RETURNED, OVERDUE)
└── renewCount

Reservation
├── reservationId (PK)
├── readerId (FK → User)
├── bookId (FK → Book)
├── reserveDate
├── notifyDate (nullable) ← ngày hệ thống thông báo sách khả dụng
├── expireDate (nullable) ← hạn chót đến lấy sách
├── status: ENUM(WAITING, NOTIFIED, FULFILLED, CANCELLED, EXPIRED)
└── queuePosition

Penalty
├── penaltyId (PK)
├── borrowId (FK → BorrowRecord)
├── readerId (FK → User)
├── type: ENUM(OVERDUE, DAMAGED, LOST)
├── amount
├── status: ENUM(UNPAID, PAID)
├── createdAt
└── paidAt (nullable)

Author
├── authorId (PK)
└── authorName

Category
├── categoryId (PK)
└── categoryName

Publisher
├── publisherId (PK)
└── publisherName

SystemConfig
├── configKey (PK)
└── configValue
```

### 5.2 Các configKey quan trọng

| Key | Ý nghĩa | Giá trị mẫu |
|---|---|---|
| `default_borrow_days` | Số ngày mượn mặc định | `14` |
| `max_renew_count` | Số lần gia hạn tối đa | `2` |
| `fine_per_day` | Mức phạt mỗi ngày trễ | `5000` (VND) |
| `reservation_hold_days` | Số ngày giữ chỗ sau khi thông báo | `3` |

---

## 6. CÁC QUY TẮC NGHIỆP VỤ QUAN TRỌNG

> Đây là các ràng buộc bắt buộc phải xử lý đúng — AI cần đặc biệt chú ý:

1. **Mượn sách:** Reader phải ở trạng thái `ACTIVE` và **không có Penalty chưa thanh toán** thì mới được mượn.
2. **Đặt trước:** Chỉ được đặt trước khi **tất cả** `BookCopy` của đầu sách đó đang ở trạng thái `BORROWED`. Nếu còn bản AVAILABLE thì phải mượn trực tiếp.
3. **Gia hạn:** Không gia hạn nếu có Reservation đang ở trạng thái `WAITING` cho đầu sách đó.
4. **Trả sách:** Khi trả phải kiểm tra tình trạng sách trước rồi mới cập nhật trạng thái BookCopy.
5. **Phạt tự động:** Hệ thống cần có scheduled job chạy hàng ngày để phát hiện và đánh dấu các BorrowRecord quá hạn (`status = OVERDUE`).
6. **Reservation queue:** Xử lý theo thứ tự FIFO dựa trên `queuePosition` hoặc `reserveDate`. Khi Reader không đến lấy trong thời hạn → tự động chuyển Reservation sang `EXPIRED` và kích hoạt người tiếp theo.
7. **Xóa sách:** Không được xóa Book/BookCopy đang có BorrowRecord ở trạng thái `BORROWING`.
8. **Khóa tài khoản:** Admin khóa tài khoản → Reader bị khóa không đăng nhập được. Librarian khóa/mở không ảnh hưởng tài khoản Admin.

---

## 7. API ENDPOINT THAM KHẢO

### 7.1 Xác thực

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
PUT    /api/auth/change-password
```

### 7.2 Người dùng (Admin)

```
GET    /api/admin/users
POST   /api/admin/users
PUT    /api/admin/users/{userId}
DELETE /api/admin/users/{userId}
PUT    /api/admin/users/{userId}/lock
PUT    /api/admin/users/{userId}/unlock
```

### 7.3 Sách

```
GET    /api/books                    ← public, hỗ trợ query params tìm kiếm
GET    /api/books/{bookId}
POST   /api/books                    ← Librarian/Admin
PUT    /api/books/{bookId}           ← Librarian/Admin
DELETE /api/books/{bookId}           ← Librarian/Admin

GET    /api/books/{bookId}/copies    ← danh sách bản sao
POST   /api/books/{bookId}/copies    ← thêm bản sao
```

### 7.4 Mượn – Trả

```
GET    /api/borrows                  ← Librarian/Admin: all; Reader: self
POST   /api/borrows                  ← Reader gửi yêu cầu mượn
PUT    /api/borrows/{borrowId}/confirm    ← Librarian xác nhận
PUT    /api/borrows/{borrowId}/return     ← Librarian xử lý trả
PUT    /api/borrows/{borrowId}/renew      ← Reader yêu cầu gia hạn
```

### 7.5 Đặt trước

```
GET    /api/reservations
POST   /api/reservations
DELETE /api/reservations/{reservationId}
```

### 7.6 Vi phạm

```
GET    /api/penalties
POST   /api/penalties                     ← Librarian tạo thủ công
PUT    /api/penalties/{penaltyId}/pay     ← Librarian đánh dấu đã nộp phạt
```

### 7.7 Thống kê

```
GET    /api/reports/borrow-stats
GET    /api/reports/top-books
GET    /api/reports/inventory
GET    /api/reports/violations
```

### 7.8 Quản trị

```
GET    /api/admin/config
PUT    /api/admin/config
GET    /api/admin/logs
POST   /api/admin/backup
```

---

## 8. YÊU CẦU PHI NGHIỆP VỤ (Non-Functional Requirements)

| Tiêu chí | Yêu cầu cụ thể |
|---|---|
| **Concurrent users** | Tối thiểu 200–500 người dùng đồng thời |
| **Response time** | ≤ 3 giây cho các chức năng chính; ≤ 10 giây cho báo cáo |
| **Uptime** | 99% uptime/tháng (downtime ≤ 1%) |
| **Availability** | 24/7, hỗ trợ hot backup |
| **Security** | BCrypt, JWT, chống SQL Injection / XSS / CSRF |
| **Browsers** | Chrome, Edge, Firefox |
| **Responsive** | Tương thích đa độ phân giải |
| **Logging** | Ghi đầy đủ thao tác quan trọng, chỉ Admin truy cập |
| **Backup** | Tự động hàng ngày/tuần, hỗ trợ restore |
| **Scalability** | Kiến trúc phân tầng, dễ mở rộng module |
| **Maintainability** | Mã nguồn chuẩn MVC, module độc lập, có tài liệu |

---

## 9. CẤU TRÚC MODULE FRONTEND (Angular)

```
src/app/
├── core/
│   ├── auth/           ← AuthService, JwtInterceptor, AuthGuard
│   ├── guards/         ← RoleGuard (READER / LIBRARIAN / ADMIN)
│   └── models/         ← TypeScript interfaces/models
│
├── shared/             ← Shared components, pipes, directives
│
├── modules/
│   ├── reader/
│   │   ├── book-search/
│   │   ├── borrow-history/
│   │   ├── my-reservations/
│   │   └── my-penalties/
│   │
│   ├── librarian/
│   │   ├── book-management/
│   │   ├── copy-management/
│   │   ├── borrow-management/
│   │   ├── reservation-management/
│   │   └── penalty-management/
│   │
│   └── admin/
│       ├── user-management/
│       ├── system-config/
│       ├── reports/
│       └── logs/
```

---

## 10. CẤU TRÚC PACKAGE BACKEND (Spring Boot)

```
src/main/java/com/library/
├── config/          ← SecurityConfig, JwtConfig, CorsConfig
├── controller/      ← REST Controllers (Auth, Book, Borrow, Reservation, ...)
├── service/         ← Business logic services
├── repository/      ← Spring Data JPA Repositories
├── entity/          ← JPA Entities (User, Book, BookCopy, ...)
├── dto/             ← Request/Response DTOs
├── exception/       ← Custom exceptions + GlobalExceptionHandler
├── security/        ← JwtUtil, JwtFilter, UserDetailsServiceImpl
└── scheduler/       ← Scheduled jobs (overdue check, reservation expiry)
```

---

## 11. KẾ HOẠCH TRIỂN KHAI GỢI Ý

### Phase 1 — Nền tảng cốt lõi
- [ ] Setup project (Spring Boot + Angular + Database)
- [ ] Entity & Database schema
- [ ] Xác thực: Register, Login, JWT
- [ ] CRUD Book, Author, Category, Publisher
- [ ] CRUD BookCopy

### Phase 2 — Nghiệp vụ mượn trả
- [ ] Mượn sách (F-40, F-41)
- [ ] Trả sách (F-50, F-51)
- [ ] Gia hạn (F-55, F-56)
- [ ] Scheduled job kiểm tra quá hạn

### Phase 3 — Đặt trước & Vi phạm
- [ ] Đặt trước sách + Queue FIFO (F-60 → F-62)
- [ ] Xử phạt vi phạm (F-70 → F-73)
- [ ] Tích hợp Notification System (email)

### Phase 4 — Quản trị & Báo cáo
- [ ] Tra cứu nâng cao (F-80, F-81)
- [ ] Thống kê & báo cáo (F-90 → F-95)
- [ ] Quản trị hệ thống: config, log, backup (F-100 → F-103)
- [ ] Quản lý người dùng đầy đủ (F-10 → F-14)

### Phase 5 — Hoàn thiện
- [ ] UI/UX hoàn chỉnh cho 3 role
- [ ] Kiểm thử toàn diện
- [ ] Tối ưu hiệu năng (index, caching)
- [ ] Tài liệu triển khai

---

*Tài liệu này là nguồn sự thật duy nhất (single source of truth) cho việc triển khai hệ thống. Mọi quyết định thiết kế cần đối chiếu với tài liệu này.*