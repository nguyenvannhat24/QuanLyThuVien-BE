package com.dev.penalty.service;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.repository.BorrowRepository;
import com.dev.config.service.SystemConfigService;
import com.dev.notification.model.NotificationType;
import com.dev.notification.service.NotificationService;
import com.dev.penalty.dto.PenaltyRequest;
import com.dev.penalty.dto.PenaltyResponse;
import com.dev.penalty.model.Penalty;
import com.dev.penalty.model.PenaltyStatus;
import com.dev.penalty.model.PenaltyType;
import com.dev.penalty.repository.PenaltyRepository;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PenaltyResponse createOverduePenalty(Borrow borrow) {
        BigDecimal fine = calculateOverdueFine(borrow, LocalDate.now());

        Penalty penalty = Penalty.builder()
                .borrowRecord(borrow)
                .reader(borrow.getUser())
                .type(PenaltyType.OVERDUE)
                .amount(fine)
                .status(PenaltyStatus.UNPAID)
                .createdDate(LocalDate.now())
                .build();

        penalty = penaltyRepository.save(penalty);

        String message = String.format("You have an overdue penalty of %.2f for late return.", fine);
        notificationService.createNotification(
                borrow.getUser(),
                "Overdue Penalty Created",
                message,
                NotificationType.PENALTY_CREATED
        );

        return mapToResponse(penalty);
    }

    @Override
    @Transactional
    public PenaltyResponse createManualPenalty(PenaltyRequest request) {
        if (request.getType() != PenaltyType.DAMAGED && request.getType() != PenaltyType.LOST) {
            throw new RuntimeException("Manual penalties can only be DAMAGED or LOST type");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Penalty amount must be greater than zero");
        }

        Borrow borrow = null;
        User reader = null;

        if (request.getBorrowId() != null) {
            borrow = borrowRepository.findById(request.getBorrowId())
                    .orElseThrow(() -> new RuntimeException("Borrow record not found with id: " + request.getBorrowId()));
            reader = borrow.getUser();
        } else {
            throw new RuntimeException("Borrow ID is required for manual penalties");
        }

        Penalty penalty = Penalty.builder()
                .borrowRecord(borrow)
                .reader(reader)
                .type(request.getType())
                .amount(request.getAmount())
                .status(PenaltyStatus.UNPAID)
                .createdDate(LocalDate.now())
                .notes(request.getNotes())
                .build();

        penalty = penaltyRepository.save(penalty);

        String message = String.format("You have been charged a %s penalty of %.2f.",
                request.getType().name().toLowerCase(), request.getAmount());
        notificationService.createNotification(
                reader,
                "Penalty Created",
                message,
                NotificationType.PENALTY_CREATED
        );

        return mapToResponse(penalty);
    }

    @Override
    @Transactional
    public PenaltyResponse payPenalty(Long penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Penalty not found with id: " + penaltyId));

        if (penalty.getStatus() == PenaltyStatus.PAID) {
            throw new RuntimeException("Penalty is already paid");
        }

        penalty.setStatus(PenaltyStatus.PAID);
        penalty.setPaidDate(LocalDate.now());
        penalty = penaltyRepository.save(penalty);

        return mapToResponse(penalty);
    }

    @Override
    public List<PenaltyResponse> getMyPenalties(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Penalty> penalties = penaltyRepository.findByReaderOrderByCreatedDateDescWithDetails(user);
        return penalties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnpaidPenalties(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return penaltyRepository.countByReaderAndStatus(user, PenaltyStatus.UNPAID);
    }

    @Override
    public BigDecimal calculateOverdueFine(Borrow borrow, LocalDate returnDate) {
        LocalDate dueDate = borrow.getDueDate();
        
        if (returnDate.isBefore(dueDate) || returnDate.isEqual(dueDate)) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        
        Integer finePerDay = systemConfigService.getConfigValueAsInt("fine_per_day");
        if (finePerDay == null) {
            finePerDay = 5000;
        }

        return BigDecimal.valueOf(daysOverdue).multiply(BigDecimal.valueOf(finePerDay));
    }

    private PenaltyResponse mapToResponse(Penalty penalty) {
        PenaltyResponse.PenaltyResponseBuilder builder = PenaltyResponse.builder()
                .penaltyId(penalty.getPenaltyId())
                .readerId(penalty.getReader().getId())
                .readerName(penalty.getReader().getFullName())
                .type(penalty.getType())
                .amount(penalty.getAmount())
                .status(penalty.getStatus())
                .createdDate(penalty.getCreatedDate())
                .paidDate(penalty.getPaidDate())
                .notes(penalty.getNotes());

        if (penalty.getBorrowRecord() != null) {
            builder.borrowId(penalty.getBorrowRecord().getId());
        }

        return builder.build();
    }
}
