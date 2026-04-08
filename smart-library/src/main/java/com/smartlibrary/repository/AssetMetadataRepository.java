package com.smartlibrary.repository;

import com.smartlibrary.entity.AssetMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssetMetadataRepository extends JpaRepository<AssetMetadata, UUID> {
}
