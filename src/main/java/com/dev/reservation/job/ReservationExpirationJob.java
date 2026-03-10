package com.dev.reservation.job;

import com.dev.reservation.model.Reservation;
import com.dev.reservation.model.ReservationStatus;
import com.dev.reservation.repository.ReservationRepository;
import com.dev.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationExpirationJob {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    
    @Scheduled(cron = "0 0 1 * * ?")
    public void expireNotifiedReservations() {
        List<Reservation> expired = reservationRepository
                .findByStatusAndExpireDateBeforeWithBook(ReservationStatus.NOTIFIED, LocalDate.now());
        
        for (Reservation r : expired) {
            reservationService.expireReservation(r.getReservationId());
        }
    }
}
