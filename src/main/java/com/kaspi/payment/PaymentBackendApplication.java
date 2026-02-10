package com.kaspi.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // Для асинхронных уведомлений в Telegram
public class PaymentBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaymentBackendApplication.class, args);
		System.out.println("✅ Kaspi Payment Backend запущен!");
		System.out.println("📞 API доступен по: http://localhost:8080");
	}
}