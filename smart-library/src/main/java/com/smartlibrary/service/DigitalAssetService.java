package com.smartlibrary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.smartlibrary.dto.library.LibraryAssetDto;
import com.smartlibrary.dto.library.UpdateAssetDto;
import com.smartlibrary.entity.AssetMetadata;
import com.smartlibrary.entity.DigitalAsset;
import com.smartlibrary.entity.enums.LicenseType;
import com.smartlibrary.repository.AssetMetadataRepository;
import com.smartlibrary.repository.DigitalAssetRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalAssetService {

    private final DigitalAssetRepository assetRepository;
    private final AssetMetadataRepository metadataRepository;
    private final PdfParserService pdfParserService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Value("${application.file-storage.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Папку для зберігання файлів створено: {}", fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не вдалося створити директорію для файлів.", ex);
        }
    }

    @Transactional
    public DigitalAsset uploadAsset(MultipartFile file, String title, LicenseType licenseType) {

        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Файл порожній або не є PDF документом!");
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл успішно збережено за шляхом: {}", targetLocation);

            DigitalAsset asset = new DigitalAsset();
            asset.setFilePath(targetLocation.toString());
            asset.setLicenseType(licenseType);
            asset.setStatus("PROCESSING");
            DigitalAsset savedAsset = assetRepository.save(asset);

            AssetMetadata metadata = new AssetMetadata();
            metadata.setDigitalAsset(savedAsset);
            metadata.setTitle(title);

            String extractedText = pdfParserService.extractTextForSummary(targetLocation.toString(), 5);

            var aiData = aiService.extractMetadata(extractedText);

            List<Double> textVector = aiService.generateEmbedding(extractedText);
            if (textVector != null && textVector.size() == 768) {
                float[] vectorArray = new float[768];
                for (int i = 0; i < 768; i++) {
                    vectorArray[i] = textVector.get(i).floatValue();
                }

                metadata.setEmbedding(vectorArray);
                log.info("Вектор успішно підготовлено до збереження в БД!");
            } else {
                log.warn("Не вдалося згенерувати валідний вектор для файлу");
            }

            metadata.setSummary(aiData.getSummary());
            try {
                List<String> authorsList = List.of(aiData.getAuthor());
                String jsonAuthor = objectMapper.writeValueAsString(authorsList);
                metadata.setAuthors(jsonAuthor);

                String jsonMarc21 = objectMapper.writeValueAsString(aiData.getMarc21Data());
                metadata.setMarc21Data(jsonMarc21);

            } catch (Exception e) {
                log.error("Помилка серіалізації метаданих у JSON: {}", e.getMessage());
                metadata.setAuthors("[\"Невідомий автор\"]");
                metadata.setMarc21Data("{}");
            }
            metadataRepository.save(metadata);

            log.info("Метадані успішно збережено для файлу: {}", targetLocation);

            return savedAsset;

        } catch (IOException ex) {
            throw new RuntimeException("Помилка збереження файлу " + originalFileName, ex);
        }
    }

    public Page<LibraryAssetDto> getAllAssets(int page, int size, String filter) {
        log.info("Отримання списку книг: сторінка {}, розмір {}, фільтр '{}'", page, size, filter);

        Pageable pageable = PageRequest.of(page, size, Sort.by("digitalAsset.createdAt").descending());

        Page<AssetMetadata> assetPage;

        if (filter != null && !filter.trim().isEmpty()) {
            assetPage = metadataRepository.searchByKeyword(filter.trim(), pageable);
        } else {
            assetPage = metadataRepository.findAll(pageable);
        }

        return assetPage.map(this::convertToDto);
    }

    public LibraryAssetDto getAssetById(UUID assetId) {
        AssetMetadata metadata = metadataRepository.findAll().stream()
                .filter(m -> m.getDigitalAsset().getId().equals(assetId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Книгу з ID " + assetId + " не знайдено"));

        return convertToDto(metadata);
    }

    @Transactional
    public LibraryAssetDto updateAsset(UUID assetId, UpdateAssetDto updateDto) {
        AssetMetadata metadata = metadataRepository.findAll().stream()
                .filter(m -> m.getDigitalAsset().getId().equals(assetId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Книгу з ID " + assetId + " не знайдено"));

        if (updateDto.getTitle() != null) metadata.setTitle(updateDto.getTitle());
        if (updateDto.getAuthors() != null) metadata.setAuthors(updateDto.getAuthors());
        if (updateDto.getSummary() != null) metadata.setSummary(updateDto.getSummary());

        if (updateDto.getMarc21Data() != null) {
            try {
                String jsonMarc21 = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(updateDto.getMarc21Data());
                metadata.setMarc21Data(jsonMarc21);
            } catch (Exception e) {
                log.error("Помилка конвертації MARC21 при оновленні: {}", e.getMessage());
            }
        }

        metadataRepository.save(metadata);
        return convertToDto(metadata);
    }

    @Transactional
    public void deleteAsset(UUID assetId) {
        DigitalAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Книгу з ID " + assetId + " не знайдено"));

        try {
            Path filePath = Path.of(asset.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Файл успішно видалено з диска: {}", filePath);
        } catch (Exception e) {
            log.error("Не вдалося видалити файл з диска: {}", e.getMessage());
        }

        assetRepository.delete(asset);
        log.info("Книгу {} повністю видалено з бази даних", assetId);
    }

    private LibraryAssetDto convertToDto(AssetMetadata metadata) {
        Object parsedMarc21 = null;
        List<String> parsedAuthors = null;
        try {
            if (metadata.getMarc21Data() != null) {
                parsedMarc21 = objectMapper.readValue(metadata.getMarc21Data(), Object.class);
            }
            if (metadata.getAuthors() != null) {
                parsedAuthors = objectMapper.readValue(metadata.getAuthors(), List.class);
            }
        } catch (Exception e) {
            log.error("Помилка парсингу MARC21 для книги {}: {}", metadata.getDigitalAsset().getId(), e.getMessage());
            if (parsedAuthors == null && metadata.getAuthors() != null) {
                parsedAuthors = List.of(metadata.getAuthors());
            }
        }

        return LibraryAssetDto.builder()
                .id(metadata.getDigitalAsset().getId())
                .title(metadata.getTitle() != null ? metadata.getTitle() : "Без назви")
                .authors(parsedAuthors)
                .summary(metadata.getSummary())
                .marc21Data(parsedMarc21)
                .filePath(metadata.getDigitalAsset().getFilePath())
                .createdAt(metadata.getDigitalAsset().getCreatedAt())
                .build();
    }

    public List<LibraryAssetDto> searchBooks(String query) {
        log.info("Шукаємо книги за запитом: '{}'", query);

        List<Double> queryEmbeddingList = aiService.generateEmbedding(query);
        if (queryEmbeddingList == null || queryEmbeddingList.size() != 768) {
            throw new RuntimeException("Не вдалося згенерувати вектор для пошукового запиту");
        }

        String vectorString = queryEmbeddingList.toString();

        List<AssetMetadata> searchResults = metadataRepository.searchSimilarDocuments(vectorString);

        return searchResults.stream().map(this::convertToDto).toList();
    }

    public Resource loadFileAsResource(UUID assetId) {
        AssetMetadata metadata = metadataRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Файл не знайдено з ID: " + assetId));

        try {
            Path filePath = Paths.get(metadata.getDigitalAsset().getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Файл не знайдено або неможливо прочитати: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Помилка при формуванні шляху до файлу", ex);
        }
    }
}