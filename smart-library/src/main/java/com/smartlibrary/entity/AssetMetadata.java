package com.smartlibrary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "asset_metadata")
@Data
public class AssetMetadata {

    @Id
    private UUID assetId; // ID збігається з ID документа (відношення 1-до-1)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "asset_id")
    private DigitalAsset digitalAsset;

    @Column(nullable = false)
    private String title;

    // Використовуємо сучасну нативну підтримку JSON від Hibernate 6
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String authors;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marc21_data", columnDefinition = "jsonb")
    private String marc21Data;

    @Column(columnDefinition = "TEXT")
    private String summary;
}
