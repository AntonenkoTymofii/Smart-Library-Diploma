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
            "AND (1 - (embedding <=> cast(:queryVectorString as vector))) > 0.7  " +
            "ORDER BY embedding <=> cast(:queryVectorString as vector) " +
            "LIMIT 5", nativeQuery = true)
    List<AssetMetadata> searchSimilarDocuments(@Param("queryVectorString") String queryVectorString);

    @Query("SELECT a FROM AssetMetadata a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(CAST(a.authors AS string)) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<AssetMetadata> searchByKeyword(@Param("filter") String filter, Pageable pageable);
}