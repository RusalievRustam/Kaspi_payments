package com.kaspi.payment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDto {

    @NotBlank(message = "eventId обязателен")
    @Size(max = 100, message = "eventId не более 100 символов")
    private String eventId;

    @NotNull(message = "amount обязателен")
    @Positive(message = "amount должен быть положительным")
    private Integer amount;  // В ТЕНГЕ: 150000 = 150 000 ₸

    @NotBlank(message = "currency обязателен")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency должен быть 3 буквы")
    private String currency = "KZT";

    @NotBlank(message = "direction обязателен")
    @Pattern(regexp = "^(IN|OUT)$", message = "direction должен быть IN или OUT")
    private String direction = "IN";

    @NotBlank(message = "rawText обязателен")
    @Size(max = 1000, message = "rawText не более 1000 символов")
    private String rawText;

    @NotNull(message = "timestamp обязателен")
    @Min(value = 1_000_000_000L, message = "timestamp должен быть корректным Unix временем")
    private Long timestamp;

    @NotNull(message = "notificationId обязателен")
    @Positive(message = "notificationId должен быть положительным")
    private Integer notificationId;

    @NotBlank(message = "deviceId обязателен")
    @Size(min = 5, max = 50, message = "deviceId от 5 до 50 символов")
    private String deviceId;
}