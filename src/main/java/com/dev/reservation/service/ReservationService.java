package com.dev.reservation.service;

import com.dev.book.model.Book;
import com.dev.reservation.dto.ReservationRequest;
import com.dev.reservation.dto.ReservationResponse;
import com.dev.reservation.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {
    ReservationResponse createReservation(Long userId, ReservationRequest request);
    
    List<ReservationResponse> getMyReservations(Long userId);
    
    void cancelReservation(Long reservationId, Long userId);
    
    Optional<Reservation> getTopWaitingReservation(Book book);
    
    Optional<Reservation> getTopNotifiedReservation(Book book);
    
    void notifyNextInQueue(Book book);
    
    void expireReservation(Long reservationId);
    
    void fulfillReservation(Long reservationId);
    
    int countWaitingReservations(Book book);
}
