package com.smartlibrary.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class PdfParserService {

    /**
     * Витягує текст із PDF-файлу.
     * Щоб не перевантажувати ШІ (і не платити за мільйони токенів),
     * для генерації анотації ми можемо читати лише перші сторінки.
     */
    public String extractTextForSummary(String filePath, int maxPages) {
        log.info("Починаємо читання тексту з файлу: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Файл не знайдено: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();

            // Читаємо з 1-ї сторінки
            stripper.setStartPage(1);

            // Якщо в книзі менше сторінок, ніж наш ліміт, беремо скільки є
            stripper.setEndPage(Math.min(document.getNumberOfPages(), maxPages));

            String text = stripper.getText(document);
            log.info("Успішно прочитано {} символів", text.length());

            return text;

        } catch (IOException e) {
            log.error("Помилка під час читання PDF: {}", e.getMessage());
            throw new RuntimeException("Не вдалося прочитати текст із PDF", e);
        }
    }
}