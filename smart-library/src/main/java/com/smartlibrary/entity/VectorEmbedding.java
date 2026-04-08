package com.smartlibrary.entity;

import com.pgvector.PGvector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "vector_embeddings")
@Data
public class VectorEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private DigitalAsset digitalAsset; // Якій книзі належить цей вектор

    @Column(name = "chunk_id", nullable = false)
    private Integer chunkId; // Порядковий номер фрагмента тексту

    // Найважливіше: колонка типу vector
    @Column(columnDefinition = "vector(1536)")
    private PGvector embedding; // 1536 - це розмірність вектора моделі OpenAI
}
