package com.smartlibrary.entity;

import com.smartlibrary.entity.enums.LicenseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digital_assets")
@Data
public class DigitalAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_path", nullable = false)
    private String filePath; // Де фізично лежить PDF-файл

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false)
    private LicenseType licenseType;

    @Column(nullable = false)
    private String status; // Наприклад: "PROCESSING", "PUBLISHED", "ERROR"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
