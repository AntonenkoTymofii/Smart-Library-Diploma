package com.smartlibrary.repository;

import com.smartlibrary.entity.VectorEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, UUID> {
    List<VectorEmbedding> findByDigitalAssetId(UUID digitalAssetId);

    // Пізніше ми додамо сюди складний SQL-запит для косинусної відстані (векторного пошуку)
}