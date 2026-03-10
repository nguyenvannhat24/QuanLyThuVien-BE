package com.dev.reservation.service;

import com.dev.book.model.Book;
import com.dev.book.model.BookCopyStatus;
import com.dev.book.repository.BookCopyRepository;
import com.dev.book.repository.BookRepository;
import com.dev.config.service.SystemConfigService;
import com.dev.notification.model.NotificationType;
import com.dev.notification.service.NotificationService;
import com.dev.reservation.dto.ReservationRequest;
import com.dev.reservation.dto.ReservationResponse;
import com.dev.reservation.model.Reservation;
import com.dev.reservation.model.ReservationStatus;
import com.dev.reservation.repository.ReservationRepository;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + request.getBookId()));

        long availableCopies = bookCopyRepository.countByBookAndStatus(book, BookCopyStatus.AVAILABLE);
        if (availableCopies > 0) {
            throw new RuntimeException("Cannot create reservation: " + availableCopies + " copies are still available");
        }

        boolean hasActiveReservation = reservationRepository.existsByReaderAndBookAndStatusIn(
                user, book, List.of(ReservationStatus.WAITING, ReservationStatus.NOTIFIED));
        if (hasActiveReservation) {
            throw new RuntimeException("User already has an active reservation for this book");
        }

        long waitingCount = reservationRepository.countByBookAndStatus(book, ReservationStatus.WAITING);
        int queuePosition = (int) waitingCount + 1;

        Reservation reservation = Reservation.builder()
                .reader(user)
                .book(book)
                .status(ReservationStatus.WAITING)
                .reserveDate(LocalDate.now())
                .queuePosition(queuePosition)
                .build();

        reservation = reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    @Override
    public List<ReservationResponse> getMyReservations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Reservation> reservations = reservationRepository.findByReaderOrderByReserveDateDescWithDetails(user);
        return reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        if (!reservation.getReader().getId().equals(userId)) {
            throw new RuntimeException("User does not own this reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
            reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new RuntimeException("Reservation is already cancelled or expired");
        }

        Book book = reservation.getBook();
        int cancelledPosition = reservation.getQueuePosition();

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        List<Reservation> waitingReservations = reservationRepository
                .findByBookAndStatusOrderByQueuePositionAsc(book, ReservationStatus.WAITING);

        for (Reservation waitingReservation : waitingReservations) {
            if (waitingReservation.getQueuePosition() > cancelledPosition) {
                waitingReservation.setQueuePosition(waitingReservation.getQueuePosition() - 1);
                reservationRepository.save(waitingReservation);
            }
        }
    }

    @Override
    public Optional<Reservation> getTopWaitingReservation(Book book) {
        return reservationRepository.findTopByBookAndStatusOrderByQueuePositionAsc(book, ReservationStatus.WAITING);
    }
    
    @Override
    public Optional<Reservation> getTopNotifiedReservation(Book book) {
        return reservationRepository.findTopByBookAndStatusOrderByQueuePositionAsc(book, ReservationStatus.NOTIFIED);
    }

    @Override
    @Transactional
    public void notifyNextInQueue(Book book) {
        Optional<Reservation> topReservationOpt = getTopWaitingReservation(book);

        if (topReservationOpt.isEmpty()) {
            return;
        }

        Reservation reservation = topReservationOpt.get();

        Integer holdDays = systemConfigService.getConfigValueAsInt("reservation_hold_days");
        if (holdDays == null) {
            holdDays = 3;
        }

        reservation.setStatus(ReservationStatus.NOTIFIED);
        reservation.setNotifyDate(LocalDate.now());
        reservation.setExpireDate(LocalDate.now().plusDays(holdDays));
        reservationRepository.save(reservation);

        String message = String.format("Your reserved book '%s' is now available. Please pick it up within %d days.",
                book.getTitle(), holdDays);
        notificationService.createNotification(
                reservation.getReader(),
                "Reserved Book Available",
                message,
                NotificationType.RESERVATION_READY
        );
    }

    @Override
    @Transactional
    public void expireReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.NOTIFIED) {
            throw new RuntimeException("Only NOTIFIED reservations can be expired");
        }

        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.save(reservation);

        notifyNextInQueue(reservation.getBook());
    }

    @Override
    @Transactional
    public void fulfillReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);
    }

    @Override
    public int countWaitingReservations(Book book) {
        return (int) reservationRepository.countByBookAndStatus(book, ReservationStatus.WAITING);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .readerId(reservation.getReader().getId())
                .bookId(reservation.getBook().getId())
                .bookTitle(reservation.getBook().getTitle())
                .status(reservation.getStatus())
                .reserveDate(reservation.getReserveDate())
                .notifyDate(reservation.getNotifyDate())
                .expireDate(reservation.getExpireDate())
                .queuePosition(reservation.getQueuePosition())
                .build();
    }
}
