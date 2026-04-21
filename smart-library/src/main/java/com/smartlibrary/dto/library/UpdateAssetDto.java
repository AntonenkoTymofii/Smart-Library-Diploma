package com.smartlibrary.dto.library;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateAssetDto {
    private String title;
    private String authors;
    private String summary;
    private Map<String, String> marc21Data;
}