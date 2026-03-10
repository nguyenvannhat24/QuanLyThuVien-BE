package com.dev.penalty.model;

import lombok.*;
import jakarta.persistence.*;
import com.dev.user.model.User;
import com.dev.borrow.model.Borrow;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "penalties", indexes = {
    @Index(name = "idx_reader_id", columnList = "reader_id"),
    @Index(name = "idx_borrow_id", columnList = "borrow_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_date", columnList = "created_date")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long penaltyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", foreignKey = @ForeignKey(name = "fk_penalty_borrow"))
    private Borrow borrowRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false, foreignKey = @ForeignKey(name = "fk_penalty_reader"))
    private User reader;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PenaltyType type;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PenaltyStatus status = PenaltyStatus.UNPAID;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(length = 500)
    private String notes;
}
