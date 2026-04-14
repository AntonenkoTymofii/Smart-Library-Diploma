package com.smartlibrary.service;

import com.smartlibrary.entity.AssetMetadata;
import com.smartlibrary.entity.DigitalAsset;
import com.smartlibrary.entity.enums.LicenseType;
import com.smartlibrary.repository.AssetMetadataRepository;
import com.smartlibrary.repository.DigitalAssetRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalAssetService {

    private final DigitalAssetRepository assetRepository;
    private final AssetMetadataRepository metadataRepository;

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
            metadataRepository.save(metadata);

            return savedAsset;

        } catch (IOException ex) {
            throw new RuntimeException("Помилка збереження файлу " + originalFileName, ex);
        }
    }
}