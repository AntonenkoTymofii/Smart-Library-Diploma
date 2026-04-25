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

    public String extractTextForSummary(String filePath, int maxPages) {
        log.info("Починаємо читання тексту з файлу: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Файл не знайдено: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setStartPage(1);

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