package com.smartlibrary.dto.library;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AssetResponse {
    private UUID id;
    private String title;
    private String status;
    private String licenseType;
    private LocalDateTime createdAt;
}