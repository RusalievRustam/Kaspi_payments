package com.kaspi.payment.service;

import com.kaspi.payment.dto.PaymentEventDto;
import com.kaspi.payment.dto.PaymentResponseDto;
import com.kaspi.payment.entity.DealEntity;
import com.kaspi.payment.entity.PaymentEventEntity;
import com.kaspi.payment.repository.PaymentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentEventRepository paymentRepository;
    private final MatchingService matchingService;
    private final TelegramService telegramService;

    @Transactional
    public PaymentResponseDto processPayment(PaymentEventDto dto) {
        log.info("Processing payment: eventId={}, amount={}, device={}",
                dto.getEventId(), dto.getAmount(), dto.getDeviceId());

        try {
            // 1) Идемпотентность по eventId
            if (paymentRepository.existsByEventId(dto.getEventId())) {
                log.warn("Duplicate payment by eventId: {}", dto.getEventId());
                return PaymentResponseDto.duplicate(dto.getEventId());
            }

            // 2) Антидубль по содержимому (на случай, если Android шлёт новый eventId)
            LocalDateTime since = LocalDateTime.now().minusMinutes(2);
            boolean dupByContent = paymentRepository.existsDuplicate(dto.getRawText(), dto.getDeviceId(), since);
            if (dupByContent) {
                log.warn("Duplicate payment by content (rawText+deviceId within 2 min). eventId={}", dto.getEventId());

                PaymentEventEntity dup = new PaymentEventEntity(dto);
                dup.setStatus("DUPLICATE");
                dup.setProcessedAt(LocalDateTime.now());
                paymentRepository.save(dup);

                return PaymentResponseDto.duplicate(dto.getEventId());
            }

            // 3) Сохраняем платёж
            PaymentEventEntity payment = new PaymentEventEntity(dto);
            payment.setProcessedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            log.info("Payment saved: id={}, amount={} ₸", payment.getId(), payment.getAmount());

            // 4) Матчинг
            DealEntity matchedDeal = matchingService.findMatchingDeal(dto.getAmount());

            if (matchedDeal != null) {
                payment.setStatus("MATCHED");
                payment.setMatchedDealId(matchedDeal.getId());
                paymentRepository.save(payment);

                log.info("Deal matched: paymentId={}, dealId={}", payment.getId(), matchedDeal.getId());

                telegramService.sendMatchNotification(matchedDeal, payment);

                return PaymentResponseDto.matched(payment.getId(), matchedDeal.getId(), dto.getAmount());
            } else {
                payment.setStatus("NO_MATCH");
                paymentRepository.save(payment);

                log.info("No matching deal found for amount: {} ₸", dto.getAmount());
                return PaymentResponseDto.noMatch(payment.getId(), dto.getAmount());
            }

        } catch (Exception e) {
            log.error("Error processing payment: eventId={}, error={}",
                    dto.getEventId(), e.getMessage(), e);
            return PaymentResponseDto.error("Processing error: " + e.getMessage());
        }
    }
}