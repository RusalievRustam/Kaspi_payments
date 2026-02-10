package com.kaspi.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deals")
public class DealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    @Column(nullable = false, length = 30)
    private String status = "WAITING_PAYMENT";

    @Column(length = 100)
    private String operatorChatId;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
    @Column/*(nullable = false)*/
    private LocalDateTime createdAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    @Column/*(nullable = false)*/
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime expiresAt;

}
