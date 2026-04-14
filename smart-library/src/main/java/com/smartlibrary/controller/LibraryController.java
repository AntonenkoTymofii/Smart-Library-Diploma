package com.smartlibrary.controller;

import com.smartlibrary.dto.library.AssetResponse;
import com.smartlibrary.entity.DigitalAsset;
import com.smartlibrary.entity.enums.LicenseType;
import com.smartlibrary.service.DigitalAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final DigitalAssetService digitalAssetService;

    // Ендпоінт для завантаження PDF-книг
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetResponse> uploadAsset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("licenseType") LicenseType licenseType
    ) {
        // Передаємо файл і дані в сервіс
        DigitalAsset savedAsset = digitalAssetService.uploadAsset(file, title, licenseType);

        // Пакуємо результат у красивий DTO для відповіді
        AssetResponse response = AssetResponse.builder()
                .id(savedAsset.getId())
                .title(title)
                .status(savedAsset.getStatus())
                .licenseType(savedAsset.getLicenseType().name())
                .createdAt(savedAsset.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }
}
