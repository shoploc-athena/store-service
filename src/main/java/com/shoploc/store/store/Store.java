package com.shoploc.store.store;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String address;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StoreStatus status;

    @Column(nullable = false)
    private Integer pickupExpiryDelayMin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    protected Store() {
    }

    public Store(UUID ownerId, String name, String address, StoreStatus status, Integer pickupExpiryDelayMin) {
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.status = status;
        this.pickupExpiryDelayMin = pickupExpiryDelayMin;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public StoreStatus getStatus() { return status; }
    public Integer getPickupExpiryDelayMin() { return pickupExpiryDelayMin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }

    public void setDescription(String description) { this.description = description; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public void setStatus(StoreStatus status) { this.status = status; }
    public void setPickupExpiryDelayMin(Integer pickupExpiryDelayMin) { this.pickupExpiryDelayMin = pickupExpiryDelayMin; }
}
