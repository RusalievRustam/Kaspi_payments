package com.kaspi.payment.controller;

import com.kaspi.payment.dto.PaymentEventDto;
import com.kaspi.payment.dto.PaymentResponseDto;
import com.kaspi.payment.service.PaymentService;
import com.kaspi.payment.service.TelegramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final TelegramService telegramService;

    @Value("${kaspi.api-key}")
    private String apiKeyConfigured;

    @PostMapping("/test-payment")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody PaymentEventDto dto) {

        if (apiKeyConfigured == null || apiKeyConfigured.isBlank()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponseDto.error("Server API key is not configured"));
        }

        if (apiKey == null || !apiKeyConfigured.equals(apiKey)) {
            log.warn("Invalid API key: {}", apiKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PaymentResponseDto.error("Invalid API key"));
        }

        log.info("Received payment: eventId={}, amount={}, device={}",
                dto.getEventId(), dto.getAmount(), dto.getDeviceId());

        PaymentResponseDto response = paymentService.processPayment(dto);

        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Kaspi Payment Backend is healthy ✅");
    }

    @PostMapping("/test-telegram")
    public ResponseEntity<String> testTelegram() {
        telegramService.testTelegramConnection();
        return ResponseEntity.ok("Telegram test initiated. Check logs/telegram.");
    }
}