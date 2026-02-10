package com.kaspi.payment.repository;

import com.kaspi.payment.entity.DealEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<DealEntity, Long> {

    List<DealEntity> findByAmountAndStatus(Integer amount, String status);

    @Query("SELECT d FROM DealEntity d WHERE " +
            "d.amount BETWEEN :minAmount AND :maxAmount AND " +
            "d.status = :status AND " +
            "(d.expiresAt IS NULL OR d.expiresAt > CURRENT_TIMESTAMP)")
    List<DealEntity> findByAmountRangeAndStatus(
            @Param("minAmount") Integer minAmount,
            @Param("maxAmount") Integer maxAmount,
            @Param("status") String status);

    @Query("SELECT d FROM DealEntity d WHERE " +
            "d.status = 'WAITING_PAYMENT' AND " +
            "(d.expiresAt IS NULL OR d.expiresAt > CURRENT_TIMESTAMP)")
    List<DealEntity> findActiveDeals();
}
