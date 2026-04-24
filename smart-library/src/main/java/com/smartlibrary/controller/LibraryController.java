package com.smartlibrary.controller;

import com.smartlibrary.dto.library.AssetResponse;
import com.smartlibrary.dto.library.LibraryAssetDto;
import com.smartlibrary.dto.library.UpdateAssetDto;
import com.smartlibrary.entity.DigitalAsset;
import com.smartlibrary.entity.enums.LicenseType;
import com.smartlibrary.service.DigitalAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/assets")
    public ResponseEntity<List<LibraryAssetDto>> getAllAssets() {
        List<LibraryAssetDto> assets = digitalAssetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<LibraryAssetDto> getAssetById(@PathVariable UUID id) {
        LibraryAssetDto asset = digitalAssetService.getAssetById(id);
        return ResponseEntity.ok(asset);
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<LibraryAssetDto> updateAsset(
            @PathVariable UUID id,
            @RequestBody UpdateAssetDto updateDto) {

        LibraryAssetDto updatedAsset = digitalAssetService.updateAsset(id, updateDto);
        return ResponseEntity.ok(updatedAsset);
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable UUID id) {
        digitalAssetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assets/search")
    public ResponseEntity<List<LibraryAssetDto>> searchAssets(@RequestParam("q") String query) {
        List<LibraryAssetDto> results = digitalAssetService.searchBooks(query);
        return ResponseEntity.ok(results);
    }
}
