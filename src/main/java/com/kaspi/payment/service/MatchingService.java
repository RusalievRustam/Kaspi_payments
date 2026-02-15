package com.kaspi.payment.service;

import com.kaspi.payment.entity.DealEntity;
import com.kaspi.payment.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final DealRepository dealRepository;

    // Толерантность в ТЕНГЕ
    private static final int AMOUNT_TOLERANCE = 1; // ±1 ₸ (можешь поставить 10)

    BigDecimal tolerance = BigDecimal.valueOf(AMOUNT_TOLERANCE);

    public DealEntity findMatchingDeal(BigDecimal paymentAmount) {
        log.debug("Looking for deal with amount: {}", paymentAmount);

        List<DealEntity> exactMatches = dealRepository.findByAmountAndStatus(
                paymentAmount, "WAITING_PAYMENT");

        if (exactMatches.size() == 1) {
            log.debug("Found exact match: dealId={}", exactMatches.get(0).getId());
            return exactMatches.get(0);
        }

        if (exactMatches.size() > 1) {
            log.warn("Multiple deals found for amount {}: {}", paymentAmount, exactMatches.size());
            return exactMatches.get(0);
        }

        BigDecimal minAmount = paymentAmount.subtract(tolerance);
        BigDecimal maxAmount = paymentAmount.add(tolerance);

        List<DealEntity> fuzzyMatches = dealRepository.findByAmountRangeAndStatus(
                minAmount, maxAmount, "WAITING_PAYMENT");

        if (fuzzyMatches.size() == 1) {
            log.debug("Found fuzzy match: dealId={}, amount={}",
                    fuzzyMatches.get(0).getId(), fuzzyMatches.get(0).getAmount());
            return fuzzyMatches.get(0);
        }

        if (fuzzyMatches.size() > 1) {
            log.warn("Multiple fuzzy matches found for amount {}: {}", paymentAmount, fuzzyMatches.size());
            return fuzzyMatches.get(0);
        }

        log.debug("No matching deal found for amount: {}", paymentAmount);
        return null;
    }
}