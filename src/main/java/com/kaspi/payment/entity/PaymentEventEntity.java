package com.kaspi.payment.entity;

import com.kaspi.payment.dto.PaymentEventDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(nullable = false)
    private BigDecimal amount;  // В ТЕНГЕ

    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    @Column(nullable = false, length = 10)
    private String direction = "IN";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private Integer notificationId;

    @Column(nullable = false, length = 50)
    private String deviceId;

    @Column(nullable = false, length = 20)
    private String status = "RECEIVED"; // RECEIVED, NO_MATCH, MATCHED, ERROR, DUPLICATE

    @Column
    private Long matchedDealId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime processedAt;

    public PaymentEventEntity(PaymentEventDto dto) {
        this.eventId = dto.getEventId();
        this.amount = dto.getAmount();
        this.currency = dto.getCurrency();
        this.direction = dto.getDirection();
        this.rawText = dto.getRawText();
        this.timestamp = dto.getTimestamp();
        this.notificationId = dto.getNotificationId();
        this.deviceId = dto.getDeviceId();
    }
}