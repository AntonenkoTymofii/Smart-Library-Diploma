package com.smartlibrary.dto.library;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LibraryAssetDto {
    private UUID id;
    private String title;
    private List<String> authors;
    private String summary;
    private Object marc21Data;
    private String filePath;
    private LocalDateTime createdAt;
}