# Phase 6: Thông Báo - Email Notifications & Automated Triggers

## Tổng Quan

**Ngày bắt đầu:** 16/03/2026  
**Dự kiến hoàn thành:** 16/03/2026  
**Mục tiêu:** Triển khai hệ thống thông báo qua email và tự động tạo thông báo cho các sự kiện quan trọng  
**Branch:** `phase-6`

---

## Mục Tiêu Chi Tiết

1. Triển khai gửi email thông qua JavaMailSender với SMTP cấu hình linh hoạt
2. Tạo các template email cho các loại thông báo khác nhau
3. Triển khai Scheduled Job để tự động kiểm tra và gửi thông báo định kỳ
4. Tích hợp tạo thông báo in-app vào các business flow (mượn sách, trả sách, đặt chỗ, phạt)
5. Quản lý cấu hình email qua SystemConfig

---

## Hiện Trạng Module Notification

Module notification đã có sẵn cơ bản từ Phase 3:

### Files hiện có:
- `Notification.java` - Entity với các trường: notificationId, user, title, message, type, isRead, createdAt
- `NotificationType.java` - Enum: BORROW_REMINDER, RETURN_DUE, RESERVATION_READY, PENALTY, SYSTEM
- `NotificationRepository.java` - Repository với các method: findByUserOrderByCreatedAtDesc, countByUserAndIsRead
- `NotificationService.java` - Interface với 4 methods
- `NotificationServiceImpl.java` - Implementation cơ bản
- `NotificationController.java` - REST endpoints: GET /my, PUT /{id}/read
- `NotificationResponse.java` - DTO cho response

### Chức năng đã có:
- Tạo thông báo thủ công
- Lấy danh sách thông báo của user
- Đánh dấu đã đọc
- Đếm thông báo chưa đọc

### Chức năng cần thêm:
- Gửi email notification
- Tự động tạo thông báo khi có sự kiện
- Scheduled jobs cho các báo nhắc nhở định kỳ

---

## Tính Năng Cần Triển Khai

### 1. Cấu Hình Email với JavaMailSender

#### Files cần tạo:

**EmailConfig.java** - Cấu hình Spring Mail:
```java
@Configuration
@RequiredArgsConstructor
public class EmailConfig {
    
    @Value("${spring.mail.host}")
    private String host;
    
    @Value("${spring.mail.port}")
    private int port;
    
    @Value("${spring.mail.username}")
    private String username;
    
    @Value("${spring.mail.password}")
    private String password;
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");
        
        return mailSender;
    }
}
```

**EmailService.java** - Interface cho email service:
```java
public interface EmailService {
    void sendSimpleEmail(String to, String subject, String body);
    
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    void sendReservationReadyEmail(User user, Book book);
    
    void sendOverdueReminderEmail(User user, Borrow borrow, int daysOverdue);
    
    void sendPenaltyNotificationEmail(User user, Penalty penalty);
    
    void sendBorrowConfirmationEmail(User user, Borrow borrow);
    
    void sendReturnConfirmationEmail(User user, Borrow borrow);
}
```

**EmailServiceImpl.java** - Implementation đầy đủ:
- Sử dụng Thymeleaf hoặc StringTemplate cho email templates
- Xử lý exception khi gửi email thất bại
- Ghi log các lần gửi email

**EmailTemplate enum** - Các loại email:
```java
public enum EmailTemplate {
    BORROW_CONFIRMATION,
    RETURN_CONFIRMATION,
    OVERDUE_REMINDER,
    RESERVATION_READY,
    PENALTY_NOTIFICATION,
    RESEND_OTP,
    ACCOUNT_LOCKED
}
```

#### Files cần sửa đổi:

**pom.xml** - Thêm dependencies:
```xml
<!-- Spring Boot-starter-mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Thymeleaf for email templates -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

**application.yaml** - Thêm cấu hình mail:
```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            enable: true
```

---

### 2. Tích Hợp Notification vào Business Flows

#### 2.1. BorrowServiceImpl - Thông báo khi mượn sách:

Thêm vào `borrowBook()` method:
- Tạo in-app notification cho user
- Gửi email xác nhận mượn sách (nếu user có email và cho phép nhận email)

#### 2.2. BorrowServiceImpl - Thông báo khi trả sách:

Thêm vào `returnBorrow()` method:
- Tạo in-app notification xác nhận trả sách thành công
- Kiểm tra và notify người đặt chỗ tiếp theo (gọi ReservationService)

#### 2.3. ReservationServiceImpl - Thông báo khi có người đặt chỗ:

Thêm vào `createReservation()` method:
- Tạo in-app notification xác nhận đặt chỗ

Thêm vào `notifyNextInQueue()` method:
- Tạo in-app notification "Sách đã có sẵn, vui lòng đến mượn trong X ngày"
- Gửi email thông báo (nếu có email)

#### 2.4. PenaltyServiceImpl - Thông báo khi tạo penalty:

Thêm vào `createPenalty()` method:
- Tạo in-app notification về penalty mới
- Gửi email thông báo penalty

---

### 3. Scheduled Jobs cho Notifications

#### Files cần tạo:

**NotificationScheduler.java** - Scheduler class:
```java
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final BorrowService borrowService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // Chạy mỗi ngày lúc 9:00 sáng
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkOverdueBorrowsAndNotify() {
        // 1. Lấy danh sách borrow quá hạn
        // 2. Gửi notification nhắc nhở
        // 3. Gửi email (nếu có email)
    }

    // Chạy mỗi ngày lúc 8:00 sáng
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkUpcomingDueDateAndNotify() {
        // 1. Lấy danh sách borrow sắp đến hạn (trong vòng 2 ngày)
        // 2. Gửi notification nhắc nhở sắp đến hạn
    }

    // Chạy mỗi giờ
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void checkExpiredReservationsAndNotify() {
        // 1. Lấy danh sách reservation đã hết hạn
        // 2. Cập nhật status -> EXPIRED
        // 3. Notify người tiếp theo trong hàng đợi
    }

    // Chạy mỗi ngày lúc 10:00 sáng
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void checkUnpaidPenaltiesAndNotify() {
        // 1. Lấy danh sách penalty UNPAID quá hạn
        // 2. Gửi notification nhắc nhở thanh toán
    }
}
```

#### Files cần sửa đổi:

**DevApplication.java** - Bật scheduling:
```java
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class DevApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevApplication.class, args);
    }
}
```

---

### 4. Email Templates

#### Cấu trúc thư mục templates:
```
src/main/resources/templates/email/
├── borrow-confirmation.html
├── return-confirmation.html
├── overdue-reminder.html
├── reservation-ready.html
└── penalty-notification.html
```

#### Ví dụ template - borrow-confirmation.html:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <h2>Xác nhận mượn sách</h2>
    <p>Kính gửi <span th:text="${user.fullName}">Người dùng</span>,</p>
    <p>Bạn đã mượn thành công sách:</p>
    <ul>
        <li><strong>Tên sách:</strong> <span th:text="${book.title}">Tên sách</span></li>
        <li><strong>Mã bản sao:</strong> <span th:text="${bookCopy.copyCode}">Mã</span></li>
        <li><strong>Ngày mượn:</strong> <span th:text="${borrow.borrowDate}">Ngày</span></li>
        <li><strong>Ngày phải trả:</strong> <span th:text="${borrow.dueDate}">Ngày</span></li>
    </ul>
    <p>Vui lòng trả sách đúng hạn để tránh phạt.</p>
    <p>Trân trọng,<br/>Thư viện</p>
</body>
</html>
```

---

### 5. Cấu Hình SystemConfig cho Notifications

#### Thêm các config keys:

| Key | Mô tả | Default Value |
|-----|-------|---------------|
| email_enabled | Bật/tắt gửi email | true |
| email_from | Địa chỉ email gửi đi | noreply@library.com |
| overdue_reminder_days | Số ngày trước hạn để nhắc nhở | 2 |
| max_overdue_reminders | Số lần nhắc nhở quá hạn tối đa | 3 |
| reservation_hold_days | Số ngày giữ chỗ khi được thông báo | 3 |

---

## API Endpoints Mới

### Email Configuration (ADMIN only):
```
GET  /api/admin/email/config     - Lấy cấu hình email
PUT  /api/admin/email/config     - Cập nhật cấu hình email
POST /api/admin/email/test       - Gửi email test
```

### Notification Settings (READER):
```
GET  /api/notifications/settings     - Lấy cài đặt thông báo
PUT  /api/notifications/settings     - Cập nhật cài đặt (email_enabled, sms_enabled)
PUT  /api/notifications/read-all     - Đánh dấu tất cả đã đọc
DELETE /api/notifications/{id}        - Xóa thông báo
```

---

## Files Cần Tạo Mới

### Config:
1. `src/main/java/com/dev/config/EmailConfig.java`

### Service:
2. `src/main/java/com/dev/email/service/EmailService.java`
3. `src/main/java/com/dev/email/service/EmailServiceImpl.java`
4. `src/main/java/com/dev/email/model/EmailTemplate.java`
5. `src/main/java/com/dev/scheduler/NotificationScheduler.java`

### DTO:
6. `src/main/java/com/dev/notification/dto/NotificationSettingsRequest.java`
7. `src/main/java/com/dev/notification/dto/EmailConfigRequest.java`

### Entity (optional - lưu notification settings):
8. `src/main/java/com/dev/notification/model/UserNotificationSettings.java`

### Controller:
9. `src/main/java/com/dev/email/controller/EmailConfigController.java`
10. `src/main/java/com/dev/notification/controller/NotificationSettingsController.java`

### Templates:
11. `src/main/resources/templates/email/borrow-confirmation.html`
12. `src/main/resources/templates/email/return-confirmation.html`
13. `src/main/resources/templates/email/overdue-reminder.html`
14. `src/main/resources/templates/email/reservation-ready.html`
15. `src/main/resources/templates/email/penalty-notification.html`

---

## Files Cần Sửa Đổi

1. **pom.xml** - Thêm spring-boot-starter-mail, spring-boot-starter-thymeleaf
2. **application.yaml** - Thêm cấu hình mail
3. **DevApplication.java** - Thêm @EnableScheduling
4. **BorrowServiceImpl.java** - Thêm tạo notification sau khi mượn/trả
5. **ReservationServiceImpl.java** - Thêm notification khi đặt chỗ và notify người tiếp theo
6. **PenaltyServiceImpl.java** - Thêm notification khi tạo penalty
7. **SystemConfigServiceImpl.java** - Thêm các config keys mới cho notification
8. **User.java** - Thêm trường email, notification preferences (optional)

---

## Database Changes

### Bảng mới - user_notification_settings:
```sql
CREATE TABLE user_notification_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    overdue_reminder_enabled BOOLEAN DEFAULT TRUE,
    reservation_ready_enabled BOOLEAN DEFAULT TRUE,
    penalty_notification_enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Testing

### Manual Testing Checklist:
- [ ] Gửi email test thành công
- [ ] Nhận email xác nhận khi mượn sách
- [ ] Nhận email xác nhận khi trả sách
- [ ] Nhận email nhắc nhở sắp đến hạn
- [ ] Nhận email khi có sách đặt chỗ sẵn sàng
- [ ] Nhận email khi có penalty mới
- [ ] Nhận email nhắc nhở thanh toán penalty
- [ ] Scheduled job chạy đúng theo cron schedule
- [ ] Notification in-app hiển thị đầy đủ
- [ ] Tắt email notification trong settings hoạt động
- [ ] Đánh dấu tất cả notification đã đọc hoạt động

---

## Error Handling

1. **Email gửi thất bại**: Ghi log lỗi nhưng không ảnh hưởng đến business flow chính
2. **Invalid email**: Validation email trước khi lưu, bỏ qua nếu email không hợp lệ
3. **Scheduled job lỗi**: Try-catch trong mỗi job, ghi log và tiếp tục job tiếp theo
4. **Template not found**: Fallback về plain text email

---

## Performance Considerations

1. **Batch gửi email**: Sử dụng @Async cho việc gửi email để không blocking main thread
2. **Giới hạn tần suất**: Không gửi quá nhiều email trong một khoảng thời gian ngắn
3. **Cache email templates**: Thymeleaf template engine có thể cache templates
4. **Job scheduling**: Chạy vào ban đêm hoặc giờ thấp điểm để tránh ảnh hưởng performance

---

## Security Considerations

1. **Không expose email credentials** trong logs hoặc responses
2. **Validate email input** với regex pattern
3. **Rate limiting** cho API gửi email test
4. **Xác thực SMTP** với credentials bảo mật
5. **SSL/TLS** bắt buộc cho SMTP connection

---

## Code Quality Checklist

- [ ] Sử dụng @Async cho email sending
- [ ] Validation đầy đủ cho email inputs
- [ ] Error handling không làm crash main flow
- [ ] Logging đầy đủ cho debugging
- [ ] Sử dụng constants cho các magic strings
- [ ] Tuân thủ code conventions hiện có
- [ ] Viết unit tests cho EmailService

---

## Kế Hoạch Triển Khai

### Bước 1: Cấu hình Email
- Thêm dependencies vào pom.xml
- Tạo EmailConfig.java
- Cấu hình application.yaml
- Test gửi email đơn giản

### Bước 2: Email Service
- Tạo EmailService interface và implementation
- Tạo email templates với Thymeleaf
- Implement các method gửi email cho từng loại notification

### Bước 3: Tích hợp vào Business Flows
- Thêm notification creation vào BorrowServiceImpl
- Thêm notification creation vào ReservationServiceImpl
- Thêm notification creation vào PenaltyServiceImpl

### Bước 4: Scheduled Jobs
- Tạo NotificationScheduler
- Implement các job kiểm tra overdue, upcoming due, expired reservations

### Bước 5: Settings & API
- Tạo UserNotificationSettings entity
- Tạo API endpoints cho notification settings
- Update controller để hỗ trợ mark-all-read, delete

### Bước 6: Testing & Documentation
- Viết unit tests
- Test tích hợp manually
- Update phase documentation

---

## Dependencies Mới

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-starter-mail | 4.0.1 | Gửi email qua SMTP |
| spring-boot-starter-thymeleaf | 4.0.1 | Email templates |

---

## Risk Assessment

### Risks thấp:
- Cấu hình email có thể không hoạt động với một số SMTP providers
- Scheduled jobs có thể bị miss nếu server down

### Mitigations:
- Cung cấp template cấu hình cho Gmail, Outlook, custom SMTP
- Ghi log khi jobs chạy để track
- Có backup mechanism (manual trigger)

---

## Estimated Time

- Cấu hình Email: 15 phút
- Email Service & Templates: 30 phút
- Tích hợp Business Flows: 20 phút
- Scheduled Jobs: 25 phút
- Settings & API: 20 phút
- Testing & Fixes: 20 phút

**Tổng:** ~130 phút (2 giờ 10 phút)

---

## Next Steps (Phase 7)

Sau khi hoàn thành Phase 6, các tính năng tiềm năng cho Phase 7:

1. **WebSocket Real-time**: Thông báo real-time lên frontend
2. **Push Notifications**: Mobile push notifications
3. **SMS Notifications**: Tin nhắn SMS cho user không có email
4. **Notification Preferences chi tiết**: Tùy chọn từng loại notification riêng
5. **Bulk Email**: Newsletter cho tất cả users

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-16  
**Author:** Development Team