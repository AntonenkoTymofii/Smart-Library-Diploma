package com.smartlibrary.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AiMetadataResponse {
    private String summary;
    private String author;
    private Map<String, String> marc21Data;
}
