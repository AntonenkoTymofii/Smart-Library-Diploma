package com.smartlibrary.repository;

import com.smartlibrary.entity.AssetMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetMetadataRepository extends JpaRepository<AssetMetadata, UUID> {
    @Query("SELECT m FROM AssetMetadata m JOIN FETCH m.digitalAsset")
    List<AssetMetadata> findAllWithDigitalAsset();
    @Query(value = "SELECT * FROM asset_metadata " +
            "WHERE embedding IS NOT NULL " +
            "ORDER BY embedding <-> cast(:queryVectorString as vector) " +
            "LIMIT 5", nativeQuery = true)
    List<AssetMetadata> searchSimilarDocuments(@Param("queryVectorString") String queryVectorString);

    Page<AssetMetadata> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(String title, String author, Pageable pageable);
}
