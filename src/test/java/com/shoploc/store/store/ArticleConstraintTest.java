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
class ArticleConstraintTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private UUID storeId;

    @BeforeEach
    void setUp() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        storeId = storeRepository.saveAndFlush(store).getId();
    }

    @Test
    void articleNegativePriceIsRejected() {
        Article article = new Article(storeId, "Test Article", -100L, ArticleType.PRODUCT);

        assertThrows(DataIntegrityViolationException.class, () -> {
            articleRepository.saveAndFlush(article);
        });
    }

    @Test
    void articleZeroPriceIsAllowed() {
        Article article = new Article(storeId, "Free Article", 0L, ArticleType.PRODUCT);

        Article saved = articleRepository.saveAndFlush(article);
        assertNotNull(saved.getId());
        assertEquals(0L, saved.getPriceCents());
    }

    @Test
    void articleDuplicateNameInSameStoreIsRejected() {
        Article article1 = new Article(storeId, "Duplicate Name", 1000L, ArticleType.PRODUCT);
        articleRepository.saveAndFlush(article1);

        Article article2 = new Article(storeId, "Duplicate Name", 2000L, ArticleType.SERVICE);

        assertThrows(DataIntegrityViolationException.class, () -> {
            articleRepository.saveAndFlush(article2);
        });
    }

    @Test
    void articleSameNameInDifferentStoresIsAllowed() {
        Store store2 = new Store(UUID.randomUUID(), "Store 2", "456 Oak St", StoreStatus.ACTIVE, 30);
        UUID storeId2 = storeRepository.saveAndFlush(store2).getId();

        Article article1 = new Article(storeId, "Same Name", 1000L, ArticleType.PRODUCT);
        Article article2 = new Article(storeId2, "Same Name", 2000L, ArticleType.PRODUCT);

        articleRepository.saveAndFlush(article1);
        Article saved2 = articleRepository.saveAndFlush(article2);

        assertNotNull(saved2.getId());
    }
}
