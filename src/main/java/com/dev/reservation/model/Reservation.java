package com.dev.reservation.model;

import lombok.*;
import jakarta.persistence.*;
import com.dev.user.model.User;
import com.dev.book.model.Book;

import java.time.LocalDate;

@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_book_id", columnList = "book_id"),
    @Index(name = "idx_reader_id", columnList = "reader_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_expire_date", columnList = "expire_date"),
    @Index(name = "idx_book_status", columnList = "book_id, status"),
    @Index(name = "idx_queue_position", columnList = "queuePosition")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_reader"))
    private User reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_book"))
    private Book book;

    @Column(nullable = false)
    private LocalDate reserveDate;

    private LocalDate notifyDate;

    private LocalDate expireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.WAITING;

    private Integer queuePosition;
}
