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
class StockConstraintTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private StockRepository stockRepository;

    private UUID articleId;

    @BeforeEach
    void setUp() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        UUID storeId = storeRepository.saveAndFlush(store).getId();

        Article article = new Article(storeId, "Test Article", 1000L, ArticleType.PRODUCT);
        articleId = articleRepository.saveAndFlush(article).getId();
    }

    @Test
    void stockNegativeAvailableQuantityIsRejected() {
        Stock stock = new Stock(articleId);
        stock.setAvailableQuantity(-1);
        stock.setReservedQuantity(0);

        assertThrows(DataIntegrityViolationException.class, () -> {
            stockRepository.saveAndFlush(stock);
        });
    }

    @Test
    void stockNegativeReservedQuantityIsRejected() {
        Stock stock = new Stock(articleId);
        stock.setAvailableQuantity(0);
        stock.setReservedQuantity(-1);

        assertThrows(DataIntegrityViolationException.class, () -> {
            stockRepository.saveAndFlush(stock);
        });
    }

    @Test
    void stockZeroQuantitiesAreAllowed() {
        Stock stock = new Stock(articleId);

        Stock saved = stockRepository.saveAndFlush(stock);
        assertNotNull(saved.getArticleId());
        assertEquals(0, saved.getAvailableQuantity());
        assertEquals(0, saved.getReservedQuantity());
    }
}
