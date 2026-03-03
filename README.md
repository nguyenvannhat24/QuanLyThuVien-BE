# dev-spring-boot

Spring Boot development environment with Docker Compose.

## Khởi chạy dự án

```bash
docker compose up -d --build
```

## Services

| Service       | URL                   | Credentials       |
| ------------- | --------------------- | ----------------- |
| Spring Boot   | http://localhost:8081 | -                 |
| Mongo Express | http://localhost:8082 | `admin` / `admin` |

danh sách các api và body cần điền
lấy danh sách book: GET http://localhost:8081/api/books?page=1&size=3
tạo book: POST http://localhost:8081/api/books/admin/create  
cập nhật : gửi tất cả các trường luôn
