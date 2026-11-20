package com.shoploc.store.store;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "store_exceptional_hours")
public class StoreExceptionalHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean isClosed;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected StoreExceptionalHours() {
    }

    public StoreExceptionalHours(UUID storeId, LocalDate startDate, LocalDate endDate, Boolean isClosed) {
        this.storeId = storeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isClosed = isClosed;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Boolean getIsClosed() { return isClosed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }
}
