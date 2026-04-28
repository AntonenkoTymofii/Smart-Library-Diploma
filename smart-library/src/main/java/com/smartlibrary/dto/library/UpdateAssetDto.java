package com.smartlibrary.dto.library;

import com.smartlibrary.entity.enums.LicenseType;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateAssetDto {
    private String title;
    private List<String> authors;
    private Integer publicationYear;
    private String summary;
    private Map<String, String> marc21Data;
    private LicenseType licenseType;
}