package com.shoploc.store.store;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long priceCents;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ArticleType type;

    @Column(nullable = false)
    private Boolean published;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;

    protected Article() {
    }

    public Article(UUID storeId, String name, Long priceCents, ArticleType type) {
        this.storeId = storeId;
        this.name = name;
        this.priceCents = priceCents;
        this.type = type;
        this.published = false;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getPriceCents() { return priceCents; }
    public ArticleType getType() { return type; }
    public Boolean getPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }

    public void setDescription(String description) { this.description = description; }
    public void setPriceCents(Long priceCents) { this.priceCents = priceCents; }
    public void setPublished(Boolean published) { this.published = published; }
}
