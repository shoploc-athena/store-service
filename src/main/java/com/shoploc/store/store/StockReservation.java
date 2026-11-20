package com.shoploc.store.store;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "stock_reservation")
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID articleId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;

    protected StockReservation() {
    }

    public StockReservation(UUID orderId, UUID articleId, Integer quantity, ReservationStatus status) {
        this.orderId = orderId;
        this.articleId = articleId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getArticleId() { return articleId; }
    public Integer getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }

    public void setStatus(ReservationStatus status) { this.status = status; }
}
