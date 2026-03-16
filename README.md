# Hệ Thống Quản Lý Thư Viện

Hệ thống quản lý thư viện được phát triển bằng Spring Boot 4.0.1, cung cấp giải pháp toàn diện cho quản lý sách, người dùng, mượn trả, đặt chỗ, phạt và báo cáo.

## Mục Lục

- [Tổng Quan](#tổng-quan)
- [Tính Năng](#tính-năng)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt](#cài-đặt)
  - [Môi Trường Development](#môi-trường-development)
  - [Môi Trường Production](#môi-trường-production)
- [Biến Môi Trường](#biến-môi-trường)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Tiến Độ Phát Triển](#tiến-độ-phát-triển)

## Tổng Quan

Hệ thống quản lý thư viện cung cấp một giải pháp hoàn chỉnh cho các thư viện hiện đại, bao gồm quản lý sách, quản lý người dùng, quy trình mượn/trả sách, hệ thống đặt chỗ, quản lý phạt, thông báo và báo cáo thống kê.

## Tính Năng

### ✅ Phase 1: Quản lý sách cơ bản
- CRUD cho sách, tác giả, nhà xuất bản, thể loại
- Quản lý bản sao sách (book copies)
- Tìm kiếm và lọc sách
- Phân trang và sắp xếp

### ✅ Phase 2: Xác thực và phân quyền
- Đăng ký và đăng nhập
- JWT authentication (access token + refresh token)
- Phân quyền theo vai trò (ADMIN, LIBRARIAN, READER)
- Spring Security integration

### ✅ Phase 3: Mượn và trả sách
- Tạo phiếu mượn sách
- Xử lý trả sách (đúng hạn/trễ hạn)
- Lịch sử mượn trả
- Gia hạn sách
- Quản lý trạng thái sách

### ✅ Phase 4: Đặt chỗ và chờ
- Đặt chỗ sách đang được mượn
- Hàng đợi ưu tiên (priority queue)
- Thông báo khi sách có sẵn
- Hủy đặt chỗ
- Quản lý thời gian giữ chỗ

### ✅ Phase 5: Phạt và thanh toán
- Tự động tính phí phạt trễ hạn
- Quản lý phiếu phạt
- Xử lý thanh toán
- Lịch sử thanh toán
- Quản lý nợ

### ✅ Phase 6: Thông báo
- Thông báo trả sách sắp đến hạn
- Thông báo sách có sẵn (đã đặt chỗ)
- Thông báo phạt
- Email notifications với JavaMailSender
- In-app notifications
- Scheduled jobs tự động gửi thông báo định kỳ
- 5 email templates (Thymeleaf): xác nhận mượn, xác nhận trả, nhắc nhở quá hạn, sách có sẵn, thông báo phạt

### ✅ Phase 7: Báo cáo và thống kê
- Báo cáo mượn/trả theo thời gian
- Thống kê sách phổ biến
- Báo cáo doanh thu phạt
- Thống kê người dùng hoạt động
- Export CSV/Excel với Apache POI
- 4 export endpoints: borrow-history, overdue, penalties, popular-books

### ✅ Phase 8: Quản trị viên
- Dashboard tổng quan
- Quản lý người dùng (block/unblock, reset password)
- Cấu hình hệ thống
- Audit logs viewer API

## Công Nghệ Sử Dụng

### Backend
- **Spring Boot**: 4.0.1
- **Java**: 17
- **Spring Security**: JWT Authentication & Authorization
- **Spring Data JPA**: ORM & Database Access
- **MySQL**: 8.0 (Production Database)
- **H2**: In-memory Database (Testing)
- **Maven**: Dependency Management

### Libraries
- **Lombok**: Reduce Boilerplate Code
- **JJWT**: JWT Token Generation & Validation
- **SpringDoc OpenAPI**: API Documentation (Swagger)
- **Apache Commons CSV**: CSV Export
- **Validation**: Bean Validation
- **AspectJ**: AOP Support

### DevOps
- **Docker**: Containerization
- **Docker Compose**: Multi-container Orchestration
- **Git**: Version Control

## Yêu Cầu Hệ Thống

- **Java**: 17 hoặc cao hơn
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Docker**: 20.10+ (optional)
- **Docker Compose**: 2.0+ (optional)

## Cài Đặt

### Môi Trường Development

#### 1. Clone repository
```bash
git clone <repository-url>
cd QuanLyThuVien-BE
```

#### 2. Cấu hình môi trường

Tạo file `.env` trong thư mục gốc:
```env
# JWT Configuration
JWT_SECRET=your-secret-key-min-256-bits-for-hs256-algorithm
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Database (for docker-compose.yml)
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=library_db
MYSQL_USER=library_user
MYSQL_PASSWORD=library_pass
```

#### 3. Khởi chạy với Docker Compose
```bash
docker compose up -d --build
```

Hoặc khởi chạy từng service:
```bash
# Chỉ khởi động MySQL và phpMyAdmin
docker compose up -d mysql phpmyadmin

# Build và chạy Spring Boot local
mvn clean install
mvn spring-boot:run
```

#### 4. Truy cập ứng dụng

| Service       | URL                   | Credentials       |
| ------------- | --------------------- | ----------------- |
| Spring Boot   | http://localhost:8081 | -                 |
| Swagger UI    | http://localhost:8081/swagger-ui.html | - |
| phpMyAdmin    | http://localhost:8082 | library_user / library_pass |

### Môi Trường Production

#### 1. Tạo file `.env.prod`

```env
# MySQL Database
MYSQL_ROOT_PASSWORD=<strong-root-password>
MYSQL_DATABASE=library_db
MYSQL_USER=library_user
MYSQL_PASSWORD=<strong-password>

# JWT Configuration
JWT_SECRET=<strong-secret-key-min-256-bits>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

#### 2. Tạo Dockerfile.prod

Tạo file `Dockerfile.prod` cho production:
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

EXPOSE 8081

# Add health check support
RUN apk add --no-cache curl

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 3. Khởi chạy production

```bash
# Load biến môi trường từ .env.prod
set -a
source .env.prod
set +a

# Khởi chạy với docker-compose.prod.yml
docker compose -f docker-compose.prod.yml up -d --build
```

#### 4. Xem logs

```bash
# Xem logs của tất cả services
docker compose -f docker-compose.prod.yml logs -f

# Xem logs của Spring Boot app
docker compose -f docker-compose.prod.yml logs -f spring-boot-app

# Xem logs của MySQL
docker compose -f docker-compose.prod.yml logs -f mysql
```

#### 5. Backup database

```bash
# Backup database
docker compose -f docker-compose.prod.yml exec mysql mysqldump -u library_user -p library_db > backup.sql

# Restore database
docker compose -f docker-compose.prod.yml exec -T mysql mysql -u library_user -p library_db < backup.sql
```

## Biến Môi Trường

### Database Configuration
| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | - | ✅ Yes |
| `MYSQL_DATABASE` | Database name | library_db | ✅ Yes |
| `MYSQL_USER` | Database user | library_user | ✅ Yes |
| `MYSQL_PASSWORD` | Database password | - | ✅ Yes |
| `SPRING_DATASOURCE_URL` | JDBC connection URL | Auto-generated | ❌ No |
| `SPRING_DATASOURCE_USERNAME` | Database username | ${MYSQL_USER} | ❌ No |
| `SPRING_DATASOURCE_PASSWORD` | Database password | ${MYSQL_PASSWORD} | ❌ No |

### JWT Configuration
| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | Secret key for JWT signing (min 256 bits) | - | ✅ Yes |
| `JWT_EXPIRATION` | Access token expiration (milliseconds) | 86400000 (24h) | ❌ No |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (milliseconds) | 604800000 (7d) | ❌ No |

### Application Configuration
| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/test/prod) | dev | ❌ No |
| `SERVER_PORT` | Application port | 8081 | ❌ No |

## API Documentation

Sau khi khởi chạy ứng dụng, truy cập Swagger UI để xem tài liệu API:

**URL**: http://localhost:8081/swagger-ui.html

### API Endpoints

#### Authentication
- `POST /api/auth/register` - Đăng ký tài khoản mới
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Đăng xuất

#### Books
- `GET /api/books` - Lấy danh sách sách (phân trang)
- `GET /api/books/{id}` - Lấy thông tin chi tiết sách
- `POST /api/books/admin/create` - Tạo sách mới (Admin)
- `PUT /api/books/admin/{id}` - Cập nhật sách (Admin)
- `DELETE /api/books/admin/{id}` - Xóa sách (Admin)

#### Borrowing
- `POST /api/borrowing/borrow` - Mượn sách
- `POST /api/borrowing/return/{id}` - Trả sách
- `POST /api/borrowing/renew/{id}` - Gia hạn sách
- `GET /api/borrowing/history` - Lịch sử mượn trả
- `GET /api/borrowing/active` - Phiếu mượn đang hoạt động

#### Reservations
- `POST /api/reservations` - Đặt chỗ sách
- `DELETE /api/reservations/{id}` - Hủy đặt chỗ
- `GET /api/reservations/my-reservations` - Danh sách đặt chỗ của tôi
- `GET /api/reservations/admin` - Quản lý đặt chỗ (Admin)

#### Penalties
- `GET /api/penalties/my-penalties` - Phiếu phạt của tôi
- `POST /api/penalties/{id}/pay` - Thanh toán phạt
- `GET /api/penalties/admin` - Quản lý phạt (Admin)

#### Categories, Authors, Publishers
- `GET /api/categories` - Danh sách thể loại
- `GET /api/authors` - Danh sách tác giả
- `GET /api/publishers` - Danh sách nhà xuất bản

Chi tiết các tham số và response xem tại Swagger UI.

## Testing

### Chạy tất cả tests
```bash
mvn test
```

### Chạy tests của một module cụ thể
```bash
mvn test -Dtest=BookServiceTest
```

### Chạy tests với coverage
```bash
mvn clean test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

## Cấu Trúc Dự Án

```
QuanLyThuVien-BE/
├── src/
│   ├── main/
│   │   ├── java/com/dev/
│   │   │   ├── auth/              # Authentication & Authorization
│   │   │   ├── book/              # Book Management (Books, Authors, Publishers, Categories)
│   │   │   ├── borrowing/         # Borrowing & Return
│   │   │   ├── reservation/       # Reservations & Waitlist
│   │   │   ├── penalty/           # Penalties & Payment
│   │   │   ├── notification/      # Notifications (Phase 6)
│   │   │   ├── report/            # Reports & Statistics (Phase 7)
│   │   │   ├── admin/             # Admin Management (Phase 8)
│   │   │   ├── config/            # Configuration Classes
│   │   │   ├── exception/         # Exception Handling
│   │   │   └── util/              # Utility Classes
│   │   └── resources/
│   │       ├── application.yaml   # Application Configuration
│   │       └── db/                # Database Scripts
│   └── test/                      # Unit & Integration Tests
├── docker-compose.yml             # Development Docker Compose
├── docker-compose.prod.yml        # Production Docker Compose
├── Dockerfile                     # Development Dockerfile
├── Dockerfile.prod                # Production Dockerfile (to be created)
├── .env                           # Environment Variables (gitignored)
└── pom.xml                        # Maven Configuration
```

## Tiến Độ Phát Triển

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Completed | Quản lý sách cơ bản |
| Phase 2 | ✅ Completed | Xác thực và phân quyền |
| Phase 3 | ✅ Completed | Mượn và trả sách |
| Phase 4 | ✅ Completed | Đặt chỗ và hàng đợi |
| Phase 5 | ✅ Completed | Phạt và thanh toán |
| Phase 6 | 🚧 In Progress | Thông báo |
| Phase 7 | 📅 Planned | Báo cáo và thống kê |
| Phase 8 | 📅 Planned | Quản trị viên nâng cao |

## License

This project is licensed under the MIT License.

## Contact

- **Email**: support@library.com
- **GitHub**: [QuanLyThuVien-BE](https://github.com/your-repo)
