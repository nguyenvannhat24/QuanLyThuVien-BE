package com.dev.email.service;

import com.dev.borrow.model.Borrow;
import com.dev.book.model.Book;
import com.dev.book.model.BookCopy;
import com.dev.penalty.model.Penalty;
import com.dev.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@library.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendBorrowConfirmationEmail(User user, Borrow borrow) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping borrow confirmation email", user.getUsername());
            return;
        }

        try {
            BookCopy bookCopy = borrow.getBookCopy();
            Book book = bookCopy.getBook();

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("book", book);
            context.setVariable("bookCopy", bookCopy);
            context.setVariable("borrow", borrow);
            context.setVariable("borrowDate", borrow.getBorrowDate().format(DATE_FORMATTER));
            context.setVariable("dueDate", borrow.getDueDate().format(DATE_FORMATTER));

            String htmlContent = templateEngine.process("email/borrow-confirmation", context);
            sendHtmlEmail(user.getEmail(), "Xác nhận mượn sách - Thư viện", htmlContent);
            log.info("Borrow confirmation email sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send borrow confirmation email to {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendReturnConfirmationEmail(User user, Borrow borrow) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping return confirmation email", user.getUsername());
            return;
        }

        try {
            BookCopy bookCopy = borrow.getBookCopy();
            Book book = bookCopy.getBook();

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("book", book);
            context.setVariable("bookCopy", bookCopy);
            context.setVariable("borrow", borrow);
            context.setVariable("returnDate", borrow.getReturnDate() != null ? 
                    borrow.getReturnDate().format(DATE_FORMATTER) : "N/A");

            String htmlContent = templateEngine.process("email/return-confirmation", context);
            sendHtmlEmail(user.getEmail(), "Xác nhận trả sách - Thư viện", htmlContent);
            log.info("Return confirmation email sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send return confirmation email to {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOverdueReminderEmail(User user, Borrow borrow, int daysOverdue) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping overdue reminder email", user.getUsername());
            return;
        }

        try {
            BookCopy bookCopy = borrow.getBookCopy();
            Book book = bookCopy.getBook();

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("book", book);
            context.setVariable("bookCopy", bookCopy);
            context.setVariable("borrow", borrow);
            context.setVariable("daysOverdue", daysOverdue);
            context.setVariable("dueDate", borrow.getDueDate().format(DATE_FORMATTER));
            context.setVariable("overdueFinePerDay", "5,000");

            String htmlContent = templateEngine.process("email/overdue-reminder", context);
            sendHtmlEmail(user.getEmail(), "Nhắc nhở sách quá hạn - Thư viện", htmlContent);
            log.info("Overdue reminder email sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send overdue reminder email to {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendReservationReadyEmail(User user, Book book) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping reservation ready email", user.getUsername());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("book", book);
            context.setVariable("holdDays", 3);

            String htmlContent = templateEngine.process("email/reservation-ready", context);
            sendHtmlEmail(user.getEmail(), "Sách đã có sẵn - Thư viện", htmlContent);
            log.info("Reservation ready email sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send reservation ready email to {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPenaltyNotificationEmail(User user, Penalty penalty) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping penalty notification email", user.getUsername());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("penalty", penalty);
            context.setVariable("penaltyDate", penalty.getCreatedDate() != null ? 
                    penalty.getCreatedDate().format(DATE_FORMATTER) : "N/A");
            context.setVariable("amount", String.format("%,d", penalty.getAmount().intValue()));

            String htmlContent = templateEngine.process("email/penalty-notification", context);
            sendHtmlEmail(user.getEmail(), "Thông báo phạt - Thư viện", htmlContent);
            log.info("Penalty notification email sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send penalty notification email to {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendReservationExpirationReminderEmail(User user, Book book, int daysRemaining) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping reservation expiration email", user.getUsername());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("book", book);
            context.setVariable("daysRemaining", daysRemaining);

            String htmlContent = templateEngine.process("email/reservation-expiration", context);
            sendHtmlEmail(user.getEmail(), "Nhắc nhở đặt chỗ sắp hết hạn - Thư viện", htmlContent);
            log.info("Reservation expiration reminder email sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send reservation expiration email to {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    public void sendTestEmail(String to) {
        String subject = "Test Email - Thư viện";
        String body = "Email test thành công! Hệ thống thông báo đang hoạt động.";
        sendSimpleEmail(to, subject, body);
        log.info("Test email sent to: {}", to);
    }
}