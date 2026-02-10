package com.kaspi.payment.repository;

import com.kaspi.payment.entity.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentEventRepository extends JpaRepository<PaymentEventEntity, Long> {

    boolean existsByEventId(String eventId);

    Optional<PaymentEventEntity> findByEventId(String eventId);

    // Проверка дубля за последние N минут
    @Query("SELECT COUNT(p) > 0 FROM PaymentEventEntity p WHERE " +
            "p.rawText = :rawText AND p.deviceId = :deviceId AND " +
            "p.createdAt >= :since")
    boolean existsDuplicate(@Param("rawText") String rawText,
                            @Param("deviceId") String deviceId,
                            @Param("since") LocalDateTime since);

    @Query("SELECT p FROM PaymentEventEntity p WHERE p.deviceId = :deviceId ORDER BY p.createdAt DESC")
    java.util.List<PaymentEventEntity> findByDeviceId(@Param("deviceId") String deviceId);
}