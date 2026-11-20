package com.shoploc.store.store;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityCoverageTest {

    @Test
    void testStoreEntityLifecycle() {
        UUID ownerId = UUID.randomUUID();
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

        store.setDescription("A store description");
        assertEquals("A store description", store.getDescription());

        store.setStatus(StoreStatus.DISABLED);
        assertEquals(StoreStatus.DISABLED, store.getStatus());

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

        StockReservation reservation = new StockReservation(orderId, articleId, quantity, ReservationStatus.RESERVED);

        assertEquals(orderId, reservation.getOrderId());
        assertEquals(articleId, reservation.getArticleId());
        assertEquals(quantity, reservation.getQuantity());
        assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
        assertNotNull(reservation.getCreatedAt());
        assertNotNull(reservation.getUpdatedAt());

        reservation.setStatus(ReservationStatus.CONSUMED);
        assertEquals(ReservationStatus.CONSUMED, reservation.getStatus());
    }

    @Test
    void testStoreOpeningHoursEntityLifecycle() {
        UUID storeId = UUID.randomUUID();
        Integer dayOfWeek = 1;
        LocalTime openTime = LocalTime.of(9, 0);
        LocalTime closeTime = LocalTime.of(18, 0);

        StoreOpeningHours hours = new StoreOpeningHours(storeId, dayOfWeek, openTime, closeTime);

        assertEquals(storeId, hours.getStoreId());
        assertEquals(dayOfWeek, hours.getDayOfWeek());
        assertEquals(openTime, hours.getOpenTime());
        assertEquals(closeTime, hours.getCloseTime());
        assertNotNull(hours.getCreatedAt());
        assertNotNull(hours.getUpdatedAt());

        LocalTime newOpen = LocalTime.of(10, 0);
        hours.setOpenTime(newOpen);
        assertEquals(newOpen, hours.getOpenTime());
    }

    @Test
    void testStoreExceptionalHoursEntityLifecycle() {
        UUID storeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 12, 25);
        LocalDate endDate = LocalDate.of(2026, 12, 25);
        Boolean isClosed = true;

        StoreExceptionalHours exceptionalHours = new StoreExceptionalHours(storeId, startDate, endDate, isClosed);

        assertEquals(storeId, exceptionalHours.getStoreId());
        assertEquals(startDate, exceptionalHours.getStartDate());
        assertEquals(endDate, exceptionalHours.getEndDate());
        assertEquals(isClosed, exceptionalHours.getIsClosed());
        assertNotNull(exceptionalHours.getCreatedAt());
        assertNotNull(exceptionalHours.getUpdatedAt());

        LocalDate newStart = LocalDate.of(2026, 12, 26);
        exceptionalHours.setStartDate(newStart);
        assertEquals(newStart, exceptionalHours.getStartDate());
    }

    @Test
    void testEnumValues() {
        assertNotNull(StoreStatus.ACTIVE);
        assertNotNull(StoreStatus.DISABLED);
        assertNotNull(ArticleType.PRODUCT);
        assertNotNull(ArticleType.SERVICE);
        assertNotNull(ReservationStatus.RESERVED);
        assertNotNull(ReservationStatus.CONSUMED);
        assertNotNull(ReservationStatus.RELEASED);
    }
}
