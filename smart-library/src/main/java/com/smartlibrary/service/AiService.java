package com.smartlibrary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlibrary.dto.AiMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;

    public AiMetadataResponse extractMetadata(String text) {
        log.info("Відправляємо текст до локального ШІ для витягування метаданих...");

        String safeText = text.length() > 1500 ? text.substring(0, 1500) : text;
        String prompt = """
                Ти професійний бібліотекар та аналітик даних. Проаналізуй текст документа і поверни метадані СУВОРО у форматі JSON.
                НЕ пиши жодних вступних слів, маркдауну чи пояснень. НЕ копіюй мої інструкції.
                
                Використовуй цей шаблон (заповни його реальними даними з тексту):
                {
                  "summary": "<Тут напиши 2-3 речення, про що цей документ>",
                  "author": "<Знайди ПІБ автора біля слів 'Виконав' або 'Студент'. Перевір чи ПІБ в називному відмінку, якщо ні то перетвори в називний відмінок. Якщо немає - 'Невідомий автор'>",
                  "year": "Знайди рік публікації та вкажи його в форматі integer. Якщо не знайшов рік, то вкажи null",
                  "marc21Data": {
                    "100": "<Тільки ПІБ автора (студента)>",
                    "245": "<Тільки назва документа або тема роботи>",
                    "260": "<Назва навчального закладу, факультет та рік (якщо знайдено)>",
                    "500": "<Тип роботи, наприклад: Практична робота №1>",
                    "520": "<Твоя анотація з поля summary>",
                    "650": "<Назва дисципліни або головні ключові слова>",
                    "700": "<ПІБ викладача, який перевірив роботу (після слова 'Перевірив')>"
                  }
                }
                
                Текст документа:
                """ + safeText;

        try {
            String response = chatModel.call(prompt);

            String cleanJson = response.replaceAll("```json", "").replaceAll("```", "").trim();

            log.info("Отримано JSON від ШІ: {}", cleanJson);

            return objectMapper.readValue(cleanJson, AiMetadataResponse.class);

        } catch (Exception e) {
            log.error("Помилка під час спілкування з ШІ або парсингу JSON: {}", e.getMessage());
            AiMetadataResponse fallback = new AiMetadataResponse();
            fallback.setSummary("Не вдалося згенерувати анотацію.");
            fallback.setAuthor("Невідомо");
            fallback.setMarc21Data(null);
            return fallback;
        }
    }

    public List<Double> generateEmbedding(String text) {
        log.info("Генеруємо векторний ембеддінг для тексту...");

        int maxLength = Math.min(text.length(), 1500);
        String safeText = text.substring(0, maxLength);

        try {
            List<Double> embedding = embeddingModel.embed(safeText);
            log.info("Ембеддінг успішно згенеровано! Розмірність вектора: {}", embedding.size());
            return embedding;
        } catch (Exception e) {
            log.error("Помилка генерації ембеддінгу: {}", e.getMessage());
            return null;
        }
    }
}