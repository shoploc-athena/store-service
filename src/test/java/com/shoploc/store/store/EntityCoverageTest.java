package com.shoploc.store.store;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityCoverageTest {

    @Test
    void testStoreEntityLifecycle() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        String name = "Test Store";
        String address = "123 Main St";

        Store store = new Store(ownerId, name, address, StoreStatus.ACTIVE, 30);

        assertEquals(ownerId, store.getOwnerId());
        assertEquals(name, store.getName());
        assertEquals(address, store.getAddress());
        assertEquals(StoreStatus.ACTIVE, store.getStatus());
        assertEquals(30, store.getPickupExpiryDelayMin());
        assertNotNull(store.getCreatedAt());
        assertNotNull(store.getUpdatedAt());
        assertNull(store.getVersion());

        store.setDescription("A store description");
        assertEquals("A store description", store.getDescription());

        store.setStatus(StoreStatus.CLOSED);
        assertEquals(StoreStatus.CLOSED, store.getStatus());

        store.setPickupExpiryDelayMin(45);
        assertEquals(45, store.getPickupExpiryDelayMin());
    }

    @Test
    void testArticleEntityLifecycle() {
        UUID storeId = UUID.randomUUID();
        String articleName = "Test Article";
        Long priceCents = 1999L;

        Article article = new Article(storeId, articleName, priceCents, ArticleType.PRODUCT);

        assertEquals(storeId, article.getStoreId());
        assertEquals(articleName, article.getName());
        assertEquals(priceCents, article.getPriceCents());
        assertEquals(ArticleType.PRODUCT, article.getType());
        assertEquals(false, article.getPublished());
        assertNotNull(article.getCreatedAt());
        assertNotNull(article.getUpdatedAt());
        assertNull(article.getVersion());

        article.setDescription("Product description");
        assertEquals("Product description", article.getDescription());

        article.setPriceCents(2500L);
        assertEquals(2500L, article.getPriceCents());

        article.setPublished(true);
        assertEquals(true, article.getPublished());
    }

    @Test
    void testStockEntityLifecycle() {
        UUID articleId = UUID.randomUUID();

        Stock stock = new Stock(articleId);

        assertEquals(articleId, stock.getArticleId());
        assertEquals(0, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertNotNull(stock.getCreatedAt());
        assertNotNull(stock.getUpdatedAt());
        assertNull(stock.getVersion());

        stock.setAvailableQuantity(100);
        assertEquals(100, stock.getAvailableQuantity());

        stock.setReservedQuantity(25);
        assertEquals(25, stock.getReservedQuantity());
    }

    @Test
    void testStockReservationEntityLifecycle() {
        UUID orderId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Integer quantity = 5;

        StockReservation reservation = new StockReservation(orderId, articleId, quantity, ReservationStatus.PENDING);

        assertEquals(orderId, reservation.getOrderId());
        assertEquals(articleId, reservation.getArticleId());
        assertEquals(quantity, reservation.getQuantity());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertNotNull(reservation.getCreatedAt());
        assertNotNull(reservation.getUpdatedAt());
        assertNull(reservation.getVersion());

        reservation.setStatus(ReservationStatus.CONFIRMED);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void testStoreOpeningHoursEntityLifecycle() {
        UUID storeId = UUID.randomUUID();

        StoreOpeningHours hours = new StoreOpeningHours(storeId, "MONDAY", "09:00", "18:00");

        assertEquals(storeId, hours.getStoreId());
        assertEquals("MONDAY", hours.getDayOfWeek());
        assertEquals("09:00", hours.getOpenTime());
        assertEquals("18:00", hours.getCloseTime());
        assertNotNull(hours.getCreatedAt());
        assertNotNull(hours.getUpdatedAt());
        assertNull(hours.getVersion());
    }

    @Test
    void testStoreExceptionalHoursEntityLifecycle() {
        UUID storeId = UUID.randomUUID();

        StoreExceptionalHours exceptionalHours = new StoreExceptionalHours(storeId, "2026-12-25", "CLOSED", null, null);

        assertEquals(storeId, exceptionalHours.getStoreId());
        assertEquals("2026-12-25", exceptionalHours.getDate());
        assertEquals("CLOSED", exceptionalHours.getStatus());
        assertNull(exceptionalHours.getOpenTime());
        assertNull(exceptionalHours.getCloseTime());
        assertNotNull(exceptionalHours.getCreatedAt());
        assertNotNull(exceptionalHours.getUpdatedAt());
        assertNull(exceptionalHours.getVersion());
    }

    @Test
    void testEnumValues() {
        assertNotNull(StoreStatus.ACTIVE);
        assertNotNull(StoreStatus.CLOSED);
        assertNotNull(ArticleType.PRODUCT);
        assertNotNull(ArticleType.SERVICE);
        assertNotNull(ReservationStatus.PENDING);
        assertNotNull(ReservationStatus.CONFIRMED);
        assertNotNull(ReservationStatus.CANCELLED);
        assertNotNull(ReservationStatus.CONSUMED);
    }
}
