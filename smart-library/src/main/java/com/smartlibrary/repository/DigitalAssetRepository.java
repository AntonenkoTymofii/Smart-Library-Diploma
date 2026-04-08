package com.smartlibrary.repository;

import com.smartlibrary.entity.DigitalAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DigitalAssetRepository extends JpaRepository<DigitalAsset, UUID> {
    boolean existsByFilePath(String filePath);
}
