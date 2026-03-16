package com.dev.email.service;

import com.dev.borrow.model.Borrow;
import com.dev.notification.model.NotificationType;
import com.dev.penalty.model.Penalty;
import com.dev.user.model.User;
import com.dev.book.model.Book;

public interface EmailService {
    
    void sendSimpleEmail(String to, String subject, String body);
    
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    void sendBorrowConfirmationEmail(User user, Borrow borrow);
    
    void sendReturnConfirmationEmail(User user, Borrow borrow);
    
    void sendOverdueReminderEmail(User user, Borrow borrow, int daysOverdue);
    
    void sendReservationReadyEmail(User user, Book book);
    
    void sendPenaltyNotificationEmail(User user, Penalty penalty);
    
    void sendReservationExpirationReminderEmail(User user, Book book, int daysRemaining);
    
    void sendTestEmail(String to);
}