package com.kaspi.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaspi.payment.entity.DealEntity;
import com.kaspi.payment.entity.PaymentEventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "telegram.bot", name = "token")
public class TelegramService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.default.chat-id:}")
    private String defaultChatId;

    @Value("${telegram.autodiscover.enabled:false}")
    private boolean autodiscoverEnabled;

    public void testTelegramConnection() {
        if (botToken == null || botToken.isEmpty()) {
            log.error("❌ Telegram токен не настроен!");
            log.error("Добавьте в application.properties:");
            log.error("telegram.bot.token=ВАШ_ТОКЕН");
            log.error("telegram.default.chat-id=ВАШ_CHAT_ID");
            return;
        }

        String testMessage = "🔧 *Тест связи Kaspi Backend*\n\n" +
                "✅ Spring Boot: запущено\n" +
                "⏰ Время: " + java.time.LocalDateTime.now();

        boolean success = sendTelegramMessage(defaultChatId, testMessage);
        if (success) log.info("✅ Телеграм тест: УСПЕХ! чат={}", defaultChatId);
        else log.error("❌ Телеграм тест: ОШИБКА");
    }

    @Async
    public void sendMatchNotification(DealEntity deal, PaymentEventEntity payment) {
        String chatId = (deal.getOperatorChatId() != null && !deal.getOperatorChatId().isBlank())
                ? deal.getOperatorChatId()
                : defaultChatId;

        if (chatId == null || chatId.isBlank()) {
            log.warn("⚠️ Chat ID не настроен для сделки {}", deal.getId());
            return;
        }

        try {
            String message = formatMessage(deal, payment);
            boolean sent = sendTelegramMessage(chatId, message);

            if (sent) log.info("✅ Telegram отправлен для сделки #{}", deal.getId());
            else log.error("❌ Не удалось отправить Telegram для сделки #{}", deal.getId());

        } catch (Exception e) {
            log.error("❌ Ошибка отправки Telegram: {}", e.getMessage(), e);
        }
    }

    private String formatMessage(DealEntity deal, PaymentEventEntity payment) {
        // amount В ТЕНГЕ
        String time = java.time.Instant.ofEpochSecond(payment.getTimestamp())
                .atZone(java.time.ZoneId.of("Asia/Almaty"))
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        return String.format(
                "✅ *ПЛАТЁЖ НАЙДЕН*\n\n" +
                        "📊 *Сделка:* #%d\n" +
                        "💵 *Сумма:* %d ₸\n" +
                        "📱 *Устройство:* %s\n" +
                        "⏰ *Время:* %s\n\n" +
                        "📋 *Текст уведомления:*\n`%s`\n\n" +
                        "_Система НЕ подтверждает сделку автоматически. Подтверди вручную._",
                deal.getId(),
                payment.getAmount(),
                payment.getDeviceId(),
                time,
                payment.getRawText()
        );
    }

    private boolean sendTelegramMessage(String chatId, String text) {
        try {
            if (botToken == null || botToken.isBlank()) {
                log.error("Telegram token is empty");
                return false;
            }

            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

            String chatIdParam;
            try {
                Long.parseLong(chatId);
                chatIdParam = chatId;
            } catch (NumberFormatException e) {
                chatIdParam = "\"" + chatId + "\"";
            }

            String json = String.format(
                    "{\"chat_id\":%s,\"text\":%s,\"parse_mode\":\"Markdown\"}",
                    chatIdParam, escapeJson(text)
            );

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Telegram API response: {} - {}", response.statusCode(), response.body());
            return response.statusCode() == 200;

        } catch (Exception e) {
            log.error("Telegram send error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Component
    public class TelegramAutoConfig implements ApplicationRunner {
        @Override
        public void run(ApplicationArguments args) {
            if (!autodiscoverEnabled) {
                log.info("Telegram autodiscover disabled");
                return;
            }
            discoverAvailableChats();
        }

        private void discoverAvailableChats() {
            try {
                if (botToken == null || botToken.isBlank()) return;

                String url = String.format("https://api.telegram.org/bot%s/getUpdates", botToken);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = new ObjectMapper().readTree(response.body());
                    JsonNode result = root.get("result");

                    log.info("=== Найденные чаты для бота ===");

                    if (result != null && result.isArray() && result.size() > 0) {
                        for (JsonNode update : result) {
                            JsonNode message = update.get("message");
                            if (message == null) continue;

                            JsonNode chat = message.get("chat");
                            if (chat == null) continue;

                            long chatId = chat.get("id").asLong();
                            String type = chat.get("type").asText();
                            String title = chat.has("title") ? chat.get("title").asText()
                                    : chat.has("first_name") ? chat.get("first_name").asText()
                                    : "unknown";

                            log.info("💬 Чат: {} (ID: {}, Тип: {})", title, chatId, type);
                        }
                    } else {
                        log.warn("⚠️ Бот не имеет доступа ни к одному чату (нет getUpdates)");
                    }
                }

            } catch (Exception e) {
                log.error("Ошибка при поиске чатов: {}", e.getMessage(), e);
            }
        }
    }

    private String escapeJson(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}