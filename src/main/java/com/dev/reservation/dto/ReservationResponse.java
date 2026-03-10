package com.dev.reservation.dto;

import com.dev.reservation.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
    private Long reservationId;
    private Long readerId;
    private Long bookId;
    private String bookTitle;
    private LocalDate reserveDate;
    private LocalDate notifyDate;
    private LocalDate expireDate;
    private ReservationStatus status;
    private Integer queuePosition;
}
