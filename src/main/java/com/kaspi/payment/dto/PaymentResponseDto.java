package com.kaspi.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PaymentResponseDto {
    private boolean success;
    private String message;
    private Long paymentId;
    private Long dealId;
    private BigDecimal amount;
    private String status; // "MATCHED", "NO_MATCH", "ERROR", "DUPLICATE"

    // Factory методы для удобства
    public static PaymentResponseDto matched(Long paymentId, Long dealId, BigDecimal amount) {
        return new PaymentResponseDto(
                true, "Payment matched with deal",
                paymentId, dealId, amount, "MATCHED");
    }

    public static PaymentResponseDto noMatch(Long paymentId, BigDecimal amount) {
        return new PaymentResponseDto(
                true, "No matching deal found",
                paymentId, null, amount, "NO_MATCH");
    }

    public static PaymentResponseDto duplicate(String eventId) {
        return new PaymentResponseDto(
                true, "Duplicate payment ignored: " + eventId,
                null, null, null, "DUPLICATE");
    }

    public static PaymentResponseDto error(String error) {
        return new PaymentResponseDto(
                false, error, null, null, null, "ERROR");
    }

    public PaymentResponseDto(boolean success, String message, Long paymentId, Long dealId, BigDecimal amount, String status) {
        this.success = success;
        this.message = message;
        this.paymentId = paymentId;
        this.dealId = dealId;
        this.amount = amount;
        this.status = status;
    }
}