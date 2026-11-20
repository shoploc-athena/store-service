package com.shoploc.store.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import com.shoploc.store.TestcontainersConfiguration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StockReservationConstraintTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private StockReservationRepository reservationRepository;

    private UUID articleId;

    @BeforeEach
    void setUp() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        UUID storeId = storeRepository.saveAndFlush(store).getId();

        Article article = new Article(storeId, "Test Article", 1000L, ArticleType.PRODUCT);
        articleId = articleRepository.saveAndFlush(article).getId();
    }

    @Test
    void reservationZeroQuantityIsRejected() {
        StockReservation reservation = new StockReservation(UUID.randomUUID(), articleId, 0, ReservationStatus.RESERVED);

        assertThrows(DataIntegrityViolationException.class, () -> {
            reservationRepository.saveAndFlush(reservation);
        });
    }

    @Test
    void reservationNegativeQuantityIsRejected() {
        StockReservation reservation = new StockReservation(UUID.randomUUID(), articleId, -5, ReservationStatus.RESERVED);

        assertThrows(DataIntegrityViolationException.class, () -> {
            reservationRepository.saveAndFlush(reservation);
        });
    }

    @Test
    void reservationPositiveQuantityIsAllowed() {
        StockReservation reservation = new StockReservation(UUID.randomUUID(), articleId, 5, ReservationStatus.RESERVED);

        StockReservation saved = reservationRepository.saveAndFlush(reservation);
        assertNotNull(saved.getId());
        assertEquals(5, saved.getQuantity());
    }

    @Test
    void duplicateOrderArticleReservationIsRejected() {
        UUID orderId = UUID.randomUUID();
        StockReservation reservation1 = new StockReservation(orderId, articleId, 5, ReservationStatus.RESERVED);
        reservationRepository.saveAndFlush(reservation1);

        StockReservation reservation2 = new StockReservation(orderId, articleId, 3, ReservationStatus.RESERVED);

        assertThrows(DataIntegrityViolationException.class, () -> {
            reservationRepository.saveAndFlush(reservation2);
        });
    }
}
